package com.dev.heenasupplier.fragments

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.dev.heenasupplier.BuildConfig
import com.dev.heenasupplier.Dialogs.NoInternetDialog
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.adapters.*
import com.dev.heenasupplier.models.*
import com.dev.heenasupplier.rest.APIClient
import com.dev.heenasupplier.rest.APIInterface
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility
import com.dev.heenasupplier.utils.Utility.Companion.IMAGE_DIRECTORY_NAME
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_my_profile.*
import kotlinx.android.synthetic.main.fragment_my_profile.view.*
import kotlinx.android.synthetic.main.fragment_settings.view.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MyProfileFragment : Fragment() {
    var mView : View ?=null
    private val PERMISSION_CAMERA_EXTERNAL_STORAGE_CODE = 301
    private val PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private var uri: Uri? = null
    val MEDIA_TYPE_IMAGE = 1
    val PICK_IMAGE_FROM_GALLERY = 10
    private val CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100
    private var imagePath = ""
    var favAdded : Boolean = true
    var imageEncoded : String = ""
    lateinit var offersAndDiscountsAdapter: OffersAndDiscountsAdapter
    lateinit var serviceAdapter: ServiceAdapter
    lateinit var servicesAdapter: ServicesAdapter
    lateinit var galleryStaggeredGridAdapter: GalleryStaggeredGridAdapter
    lateinit var reviewsAdapter: ReviewsAdapter
    private val galleryPhotosShow = ArrayList<GalleryPhotosShow>()
    private val galleryPhotos = ArrayList<String>()
    private var selectedImageUriList = ArrayList<String>()
    private var ImageUriList = ArrayList<Gallery>()
    val requestOption = RequestOptions().centerCrop()
    val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
    var serviceslisting = ArrayList<Service>()
    var offersListing = ArrayList<OfferItem>()
    var commentsList = ArrayList<CommentsItem>()
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_my_profile, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if(!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    showProfile()
                    getServices()
                    getOffers()
                }

            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "My Profile Fragment")
        }

        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setOnClickListener {
            findNavController().navigate(R.id.notificationsFragment)
        }

        Glide.with(this).load(
                SharedPreferenceUtility.getInstance().get(
                        SharedPreferenceUtility.ProfilePic,
                        ""
                )
        )
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                        p0: GlideException?,
                        p1: Any?,
                        p2: com.bumptech.glide.request.target.Target<Drawable>?,
                        p3: Boolean
                ): Boolean {
                    Log.e("err", p0?.message.toString())
                    return false
                }

                override fun onResourceReady(
                        p0: Drawable?,
                        p1: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: com.bumptech.glide.load.DataSource?,
                        p4: Boolean
                ): Boolean {


                    return false
                }
            }).apply(requestOption).into(mView!!.civ_profile)


        tv_add_new_offers.setOnClickListener {
            var offer_id = 0
            var status = "add"
            val bundle = Bundle()
            bundle.putInt("offer_id", offer_id!!)
            bundle.putString("status", status)
            findNavController().navigate(R.id.action_myProfileFragment_to_addNewOffersFragment, bundle)
        }

        tv_add_new_service.setOnClickListener {
            var service_id = 0
            var status = "add"
            val bundle = Bundle()
            bundle.putInt("service_id", service_id!!)
            bundle.putString("status", status)
            findNavController().navigate(R.id.action_myProfileFragment_to_addNewFeaturedFragment, bundle)
        }

        mView!!.card_upload_photo.setOnClickListener {
            mView!!.card_upload_photo.startAnimation(AlphaAnimation(1f, 0.5f))
            requestToUploadProfilePhoto()
        }

        showProfile()
        getServices()
        getOffers()

        mView!!.tv_services.setOnClickListener {
            mView!!.naqashat_services_layout.visibility = View.VISIBLE
            mView!!.naqashat_gallery_layout.visibility = View.GONE
            mView!!.naqashat_reviews_layout.visibility = View.GONE
            getServices()
            getOffers()
            mView!!.tv_services.setBackgroundResource(R.drawable.little_gold_curved)
            mView!!.tv_gallery.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_reviews.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_services.setTextColor(resources.getColor(R.color.white))
            mView!!.tv_gallery.setTextColor(resources.getColor(R.color.gold))
            mView!!.tv_reviews.setTextColor(resources.getColor(R.color.gold))
        }

        mView!!.tv_gallery.setOnClickListener {
            mView!!.naqashat_services_layout.visibility = View.GONE
            mView!!.naqashat_gallery_layout.visibility = View.VISIBLE
            mView!!.naqashat_reviews_layout.visibility = View.GONE

            mView!!.tv_services.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_gallery.setBackgroundResource(R.drawable.little_gold_curved)
            mView!!.tv_reviews.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_services.setTextColor(resources.getColor(R.color.gold))
            mView!!.tv_gallery.setTextColor(resources.getColor(R.color.white))
            mView!!.tv_reviews.setTextColor(resources.getColor(R.color.gold))

            showGallery()
        }

        mView!!.tv_reviews.setOnClickListener {
            mView!!.naqashat_services_layout.visibility = View.GONE
            mView!!.naqashat_gallery_layout.visibility = View.GONE
            mView!!.naqashat_reviews_layout.visibility = View.VISIBLE
            getReviews()
            mView!!.tv_services.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_gallery.setBackgroundResource(R.drawable.little_curved_white_filled_gold_border_rect_box)
            mView!!.tv_reviews.setBackgroundResource(R.drawable.little_gold_curved)
            mView!!.tv_services.setTextColor(resources.getColor(R.color.gold))
            mView!!.tv_gallery.setTextColor(resources.getColor(R.color.gold))
            mView!!.tv_reviews.setTextColor(resources.getColor(R.color.white))
        }
    }

    private fun showProfile() {
        mView!!.fragment_profile_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = Utility.apiInterface.showProfile(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId,0))
        call?.enqueue(object : Callback<ProfileShowResponse?>{
            override fun onResponse(
                    call: Call<ProfileShowResponse?>,
                    response: Response<ProfileShowResponse?>
            ) {
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.body() != null) {
                        if(response.body()!!.status==1){
                            LogUtils.shortToast(requireContext(), response.body()!!.message)
                            Glide.with(requireContext()).load(response.body()!!.profile!!.image).into(mView!!.civ_profile)
                            val name_location = response.body()!!.profile!!.username + "/" + response.body()!!.profile!!.address

                            if(response.body()!!.profile!!.comment_avg.equals("")){
                                mView!!.ratingBar.rating=0F
                                mView!!.txtRating.text = "0"
                            }else{
                                mView!!.ratingBar.rating = response.body()!!.profile!!.comment_avg!!.toFloat()
                                mView!!.txtRating.text = response.body()!!.profile!!.comment_avg!!.toString()
                            }
                            mView!!.tv_naqashat_name_location.text = name_location
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ProfileShowResponse?>, throwable: Throwable) {
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
            }

        })
    }

    private fun getReviews() {
        mView!!.fragment_profile_progressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.showComments(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId,0))
        call?.enqueue(object : Callback<ShowCommentsResponse?>{
            override fun onResponse(call: Call<ShowCommentsResponse?>, response: Response<ShowCommentsResponse?>) {
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        mView!!.rv_reviews.visibility = View.VISIBLE
                        mView!!.ll_no_comments_found.visibility = View.GONE
                        commentsList = response.body()!!.comments as ArrayList<CommentsItem>
                        mView!!.rv_reviews.layoutManager = LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                        )

                        reviewsAdapter = ReviewsAdapter(
                                requireContext(),
                                commentsList)
                        mView!!.rv_reviews.adapter = reviewsAdapter
                        reviewsAdapter.notifyDataSetChanged()
                    }else{
                        mView!!.rv_reviews.visibility = View.GONE
                        mView!!.ll_no_comments_found.visibility = View.VISIBLE
                        LogUtils.shortToast(requireContext(), response.body()!!.message)
                    }
                }else{
                    mView!!.rv_reviews.visibility = View.GONE
                    mView!!.ll_no_comments_found.visibility = View.VISIBLE
                     LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<ShowCommentsResponse?>, throwable: Throwable) {
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                mView!!.rv_reviews.visibility = View.GONE
                mView!!.ll_no_comments_found.visibility = View.VISIBLE
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
            }

        })
    }

    private fun getOffers() {
        val call = apiInterface.getOffersListing(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId,0))
        call?.enqueue(object : Callback<OffersListingResponse?>{
            override fun onResponse(
                call: Call<OffersListingResponse?>,
                response: Response<OffersListingResponse?>
            ) {
                if(response.isSuccessful){
                    if (response.body()!!.status == 1){
                        offersListing = response.body()!!.offer as ArrayList<OfferItem>
                        mView!!.rv_offers_n_discs.visibility = View.VISIBLE
                        mView!!.ll_no_offers_and_disc_found.visibility = View.GONE
                        mView!!.rv_offers_n_discs.layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )

                        offersAndDiscountsAdapter = OffersAndDiscountsAdapter(
                            requireContext(),
                            offersListing,
                            object : ClickInterface.onOffersItemClick {
                                override fun onOfferDelete(position: Int) {
                                    val offerId = offersListing[position].offer_id
                                    deleteOffer(offerId!!, position)
                                }

                                override fun onOfferEdit(position: Int) {
                                    var offer_id = offersListing[position].offer_id
                                    var status = "edit"
                                    val bundle = Bundle()
                                    bundle.putInt("offer_id", offer_id!!)
                                    bundle.putString("status", status)
                                    findNavController().navigate(R.id.action_myProfileFragment_to_addNewOffersFragment, bundle)
                                }
                            })
                        mView!!.rv_offers_n_discs.adapter = offersAndDiscountsAdapter
                        offersAndDiscountsAdapter.notifyDataSetChanged()
                    }else{
                        LogUtils.shortToast(requireContext(), response.body()!!.message)
                        mView!!.rv_offers_n_discs.visibility = View.GONE
                        mView!!.ll_no_offers_and_disc_found.visibility = View.VISIBLE
                    }
                }else{
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                    mView!!.rv_offers_n_discs.visibility = View.GONE
                    mView!!.ll_no_offers_and_disc_found.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<OffersListingResponse?>, throwable: Throwable) {
                mView!!.rv_offers_n_discs.visibility = View.GONE
                mView!!.ll_no_offers_and_disc_found.visibility = View.VISIBLE
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
            }

        })
    }

    private fun deleteOffer(offerId: Int, position: Int) {
        val call = apiInterface.deleteoffer(offerId)
        call?.enqueue(object : Callback<OfferDeleteResponse?>{
            override fun onResponse(call: Call<OfferDeleteResponse?>, response: Response<OfferDeleteResponse?>) {
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        offersListing.removeAt(position)
                        if (mView!!.rv_offers_n_discs.adapter!=null){
                            mView!!.rv_offers_n_discs.adapter!!.notifyDataSetChanged()
                        }
                        if (offersListing.size==0){
                            mView!!.rv_offers_n_discs.visibility = View.GONE
                            mView!!.ll_no_offers_and_disc_found.visibility = View.VISIBLE
                        }else{
                            mView!!.rv_offers_n_discs.visibility = View.VISIBLE
                            mView!!.ll_no_offers_and_disc_found.visibility = View.GONE
                        }
                        LogUtils.longToast(requireContext(), response.body()!!.message)
                    }else{
                        LogUtils.longToast(requireContext(), response.body()!!.message)
                    }
                }else{
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<OfferDeleteResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
            }

        })
    }

    private fun getServices() {
        mView!!.fragment_profile_progressBar.visibility = View.GONE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.serviceslisting(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId,0),
                SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, ""))
        call!!.enqueue(object : Callback<ServiceListingResponse?>{
            override fun onResponse(
                call: Call<ServiceListingResponse?>,
                response: Response<ServiceListingResponse?>
            ) {
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        serviceslisting = response.body()!!.service as ArrayList<Service>
                        mView!!.rv_services_listing.visibility = View.VISIBLE
                        mView!!.ll_no_service_found.visibility = View.GONE
                        mView!!.rv_services_listing.layoutManager = LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                        )
                        servicesAdapter = ServicesAdapter(requireContext(), serviceslisting, object : ClickInterface.onServicesItemClick{
                            override fun onServicClick(position: Int) {
                                val bundle = Bundle()
                                bundle.putStringArrayList("gallery",
                                    serviceslisting[position].gallery as ArrayList<String>?
                                )
                                findNavController().navigate(R.id.viewImageFragment, bundle)
                            }

                            override fun onServiceDele(position: Int) {
                                val service_id = serviceslisting.get(position).service_id
                                deleteServices(service_id!!, position)
                            }

                            override fun onServiceEdit(position: Int) {
                                var service_id = serviceslisting.get(position).service_id
                                var status = "edit"
                                val bundle = Bundle()
                                bundle.putInt("service_id", service_id!!)
                                bundle.putString("status", status)
                                findNavController().navigate(R.id.action_myProfileFragment_to_addNewFeaturedFragment, bundle)
                            }

                        })
                        mView!!.rv_services_listing.adapter = servicesAdapter
                        servicesAdapter.notifyDataSetChanged()
                    }else{
                        mView!!.rv_services_listing.visibility = View.GONE
                        mView!!.ll_no_service_found.visibility = View.VISIBLE
                    }
                }else{
                    mView!!.rv_services_listing.visibility = View.GONE
                    mView!!.ll_no_service_found.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<ServiceListingResponse?>, throwable: Throwable) {
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                mView!!.rv_services_listing.visibility = View.GONE
                mView!!.ll_no_service_found.visibility = View.VISIBLE
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), throwable.message)
            }

        })
    }

    private fun deleteServices(serviceId: Int, position: Int) {
        mView!!.fragment_profile_progressBar.visibility = View.GONE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val call = apiInterface.deleteservice(serviceId)
        call!!.enqueue(object : Callback<DeleteServiceResponse?>{
            override fun onResponse(
                call: Call<DeleteServiceResponse?>,
                response: Response<DeleteServiceResponse?>
            ) {
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        serviceslisting.removeAt(position)
                        if (mView!!.rv_services_listing.adapter != null) {
                            mView!!.rv_services_listing.adapter!!.notifyDataSetChanged()
                        }
                        if (serviceslisting.size==0){
                            mView!!.rv_services_listing.visibility = View.GONE
                            mView!!.ll_no_service_found.visibility = View.VISIBLE
                        }else{
                            mView!!.rv_services_listing.visibility = View.VISIBLE
                            mView!!.ll_no_service_found.visibility = View.GONE
                        }
                        LogUtils.longToast(requireContext(), response.body()!!.message)
                    }
                }else{
                    LogUtils.shortToast(requireContext(), getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<DeleteServiceResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                mView!!.fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
            }

        })
    }

    private fun showGallery() {
        requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        fragment_profile_progressBar.visibility = View.VISIBLE

        val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
        val call = apiInterface.galleryListing(
                SharedPreferenceUtility.getInstance().get(
                        SharedPreferenceUtility.UserId,
                        0
                )
        )
        call!!.enqueue(object : Callback<GalleryListResponse?> {
            override fun onResponse(
                    call: Call<GalleryListResponse?>,
                    response: Response<GalleryListResponse?>
            ) {
                fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful) {
                    if (response.body()!!.status == 1) {
                        mView!!.rv_naqashat_gallery.visibility = View.VISIBLE
                        mView!!.ll_no_gallery_found.visibility = View.GONE
                        mView!!.rv_naqashat_gallery.layoutManager = GridLayoutManager(
                                requireContext(),
                                2,
                                GridLayoutManager.VERTICAL,
                                false
                        )
                        ImageUriList = response.body()!!.gallery as ArrayList<Gallery>
                        galleryStaggeredGridAdapter = GalleryStaggeredGridAdapter(
                                requireContext(),
                                ImageUriList,
                                object : ClickInterface.OnRecyclerItemClick {
                                    override fun OnClickAction(position: Int) {
                                        val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
                                        val call = apiInterface.deletegalleryimage(ImageUriList[position].gallery_id)
                                        call!!.enqueue(object : Callback<DeleteGalleryImage?> {
                                            override fun onResponse(
                                                    call: Call<DeleteGalleryImage?>,
                                                    response: Response<DeleteGalleryImage?>
                                            ) {
                                                try {
                                                    if (response.isSuccessful) {
                                                        if (response.body()!!.status == 1) {
                                                            ImageUriList.removeAt(position)
                                                            if (mView!!.rv_naqashat_gallery.adapter != null) {
                                                                mView!!.rv_naqashat_gallery.adapter!!.notifyDataSetChanged()
                                                            }
                                                            if (ImageUriList.size==0){
                                                                mView!!.rv_naqashat_gallery.visibility = View.GONE
                                                                mView!!.ll_no_gallery_found.visibility = View.VISIBLE
                                                            }else{
                                                                mView!!.rv_naqashat_gallery.visibility = View.VISIBLE
                                                                mView!!.ll_no_gallery_found.visibility = View.GONE
                                                            }
                                                            LogUtils.longToast(requireContext(), response.body()!!.message)

                                                        } else {
                                                            LogUtils.longToast(requireContext(), response.body()!!.message)
                                                        }
                                                    } else {
                                                        LogUtils.longToast(requireContext(), requireContext().getString(R.string.response_isnt_successful))
                                                    }
                                                } catch (e: IOException) {
                                                    e.printStackTrace()
                                                } catch (e: JSONException) {
                                                    e.printStackTrace()
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }

                                            override fun onFailure(call: Call<DeleteGalleryImage?>, throwable: Throwable) {
                                                LogUtils.e("msg", throwable.message)
                                                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                                            }
                                        })
                                    }

                                }
                        )
                        mView!!.rv_naqashat_gallery.adapter = galleryStaggeredGridAdapter
                        galleryStaggeredGridAdapter.notifyDataSetChanged()
                    } else {
                        mView!!.rv_naqashat_gallery.visibility = View.GONE
                        mView!!.ll_no_gallery_found.visibility = View.VISIBLE
                        LogUtils.longToast(requireContext(), response.body()!!.message)
                    }
                } else {
                    mView!!.rv_naqashat_gallery.visibility = View.GONE
                    mView!!.ll_no_gallery_found.visibility = View.VISIBLE
                    LogUtils.shortToast(
                            requireContext(),
                            getString(R.string.response_isnt_successful)
                    )
                }
            }

            override fun onFailure(call: Call<GalleryListResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView!!.rv_naqashat_gallery.visibility = View.GONE
                mView!!.ll_no_gallery_found.visibility = View.VISIBLE
                fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun requestToUploadProfilePhoto() {
        if (!hasPermissions(requireActivity(), *PERMISSIONS)) {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    PERMISSIONS,
                    PERMISSION_CAMERA_EXTERNAL_STORAGE_CODE
            )
        } else if (hasPermissions(requireActivity(), *PERMISSIONS)) {
            openCameraDialog()
        }
    }

    private fun openCameraDialog() {
        val items = arrayOf<CharSequence>(
                getString(R.string.camera), getString(R.string.gallery), getString(
                R.string.cancel
        )
        )
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(getString(R.string.add_photo))
        builder.setItems(items) { dialogInterface, i ->
            if (items[i] == getString(R.string.camera)) {
                captureImage()
            } else if (items[i] == getString(R.string.gallery)) {
                chooseImage()
            } else if (items[i] == getString(R.string.cancel)) {
                dialogInterface.dismiss()
            }
        }
        builder.show()
    }

    private fun captureImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        uri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE)


    }

    fun getOutputMediaFileUri(type: Int): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                    requireActivity(),
                    BuildConfig.APPLICATION_ID.toString() + ".provider",
                    getOutputMediaFile(
                            type
                    )!!
            )
        } else {
            Uri.fromFile(getOutputMediaFile(type))
        }
    }

    private fun getOutputMediaFile(type: Int): File? {
        val mediaStorageDir = File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME
        )
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs()
        }
        // Create a media file name
        val timeStamp = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
        ).format(Date())
        val mediaFile: File
        mediaFile = if (type == MEDIA_TYPE_IMAGE) {
            File(
                    mediaStorageDir.path + File.separator
                            + "IMG_" + timeStamp + ".png"
            )
        } else if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            File(
                    mediaStorageDir.path + File.separator
                            + "VID_" + timeStamp + ".mp4"
            )
        } else {
            return null
        }
        return mediaFile
    }
    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, PICK_IMAGE_FROM_GALLERY)
    }

    fun hasAllPermissionsGranted(grantResults: IntArray): Boolean {
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CAMERA_EXTERNAL_STORAGE_CODE) {
            if (grantResults.size > 0) { /*  if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {*/
                if (hasAllPermissionsGranted(grantResults)) {
                    openCameraDialog()
                } else {
                    LogUtils.shortToast(
                            requireContext(),
                            "Please grant both Camera and Storage permissions"
                    )

                }
            } else if (!hasAllPermissionsGranted(grantResults)) {
                LogUtils.shortToast(
                        requireContext(),
                        "Please grant both Camera and Storage permissions"
                )
            }
        }
    }

    fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
        if (context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission!!) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        } else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) { //previewCapturedImage();
                if (uri != null) {
//                    galleryPhotos.clear()
                    imagePath = ""
                    Log.e("uri", uri.toString())
                    imagePath = uri!!.path!!
                    galleryPhotos.add(imagePath)
                    setUploadPhotos(galleryPhotos)
                    //Glide.with(this).load("file:///$imagePath").placeholder(R.drawable.user).into(civ_profile)
                } else {
                    LogUtils.shortToast(requireContext(), "something went wrong! please try again")
                }
            }
        } else if (requestCode == PICK_IMAGE_FROM_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            galleryPhotos.clear()
            imagePath = ""
            if (data.clipData != null) {
                val mClipData: ClipData = data.clipData!!
                val cout: Int = data.clipData!!.itemCount
                for (i in 0 until cout) {
                    // adding imageuri in array
                    val imageurl: Uri = data.clipData!!.getItemAt(i).uri
                    if (imageurl.toString().startsWith("content")) {
                        imagePath = getRealPath(imageurl)!!
                    } else {
                        imagePath = imageurl.getPath()!!
                    }
                    galleryPhotos.add(imagePath)
                }
                if (galleryPhotos.size==0){
                    LogUtils.shortToast(requireContext(), getString(R.string.please_select_atleast_one_image_to_proceed))
                }else{
                    setUploadPhotos(galleryPhotos)
                }

            } else if (data.data!=null) {
                /*  val imageurl: Uri = (data.clipData as Nothing?)!!
                  mArrayUri!!.add(imageurl)
                  pathList.add(imageurl.toString())
                  if(pathList.size==5){
                      mView!!.imgAttach.alpha=0.5f
                      mView!!.imgAttach.isEnabled=false
                  }
                  else{
                      mView!!.imgAttach.alpha=1f
                      mView!!.imgAttach.isEnabled=true
                  }
                  uploadImageVideoAdapter.notifyDataSetChanged()*/

                val imagePath = data.data!!.path
                galleryPhotos.add(imagePath.toString())
            }
        }
    }

    private fun getRealPath(ur: Uri?): String? {
        var realpath = ""
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

        // Get the cursor
        val cursor: Cursor = requireContext().contentResolver.query(ur!!,
                filePathColumn, null, null, null
        )!!
        cursor.moveToFirst()
        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
        //Log.e("columnIndex", String.valueOf(MediaStore.Images.Media.DATA));
        realpath = cursor.getString(columnIndex)
        cursor.close()
        return realpath
    }

    private fun setUploadPhotos(galleryPhotos: ArrayList<String>) {
        val apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
        val builder = APIClient.createMultipartBodyBuilder(
                arrayOf("user_id"),
                arrayOf(
                        SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0)
                                .toString()
                )
        )

        // Map is used to multipart the file using okhttp3.RequestBody
        // Multiple Images
        // Map is used to multipart the file using okhttp3.RequestBody
        // Multiple Images
        for (i in 0 until galleryPhotos.size) {
            val file: File = File(galleryPhotos.get(i))
            builder!!.addFormDataPart(
                    "image[]",
                    file.name,
                    RequestBody.create(MediaType.parse("image/*"), file)
            )
        }

        val call = apiInterface.uploadGallery(builder!!.build())
        call!!.enqueue(object : Callback<UploadGalleryResponse?> {
            override fun onResponse(
                    call: Call<UploadGalleryResponse?>,
                    response: Response<UploadGalleryResponse?>
            ) {
                fragment_profile_progressBar.visibility = View.VISIBLE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful) {
                        if (response.body()!!.status == 1) {
                            fragment_profile_progressBar.visibility = View.GONE
                            LogUtils.longToast(requireContext(), response.body()!!.message)
                            showGallery()
                        } else {
                            LogUtils.longToast(requireContext(), response.body()!!.message)
                        }
                    } else {
                        LogUtils.longToast(
                                requireContext(),
                                getString(R.string.response_isnt_successful)
                        )
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<UploadGalleryResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), throwable.localizedMessage)
                fragment_profile_progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    override fun onResume() {
        super.onResume()
        getServices()
        getOffers()
        galleryPhotos.clear()
    }

    companion object{
        private var instance: SharedPreferenceUtility? = null
        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }
}