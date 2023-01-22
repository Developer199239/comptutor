package com.example.comptutor.utils

import android.app.AlertDialog
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.DialogFragment
import com.example.comptutor.databinding.DialogEmbeddedVideoLinkPlayBinding

class EmbeddVideoLinkPlayDialog : DialogFragment() {
    private var _binding: DialogEmbeddedVideoLinkPlayBinding? = null
    private val binding get() = _binding!!
    private lateinit var materialProgress: MaterialProgress
    private lateinit var sessionHelper: SessionHelper
    private var videoLink: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEmbeddedVideoLinkPlayBinding.inflate(layoutInflater)
        initView()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView() {
        sessionHelper = SessionHelper(requireContext())
        materialProgress = MaterialProgress(requireActivity())

        binding.ivCross.setOnClickListener {
            dismiss()
            (requireActivity() as BaseActivity).showAlertForVideoWatchingFinish()
        }

        val webSettings: WebSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowFileAccess = true

        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.mixedContentMode = 0
            binding.webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            binding.webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        binding.webView.settings.builtInZoomControls = true
        binding.webView.settings.loadWithOverviewMode = true
        binding.webView.settings.useWideViewPort = true
        binding.webView.settings.displayZoomControls = false
        binding.webView.webChromeClient = WebChromeClient()
        binding.webView.settings.loadsImagesAutomatically = true
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.allowFileAccess = true
        binding.webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        binding.webView.settings.javaScriptCanOpenWindowsAutomatically = true
        binding.webView.settings.pluginState = WebSettings.PluginState.ON
        binding.webView.settings.mediaPlaybackRequiresUserGesture = false
        binding.webView.settings.domStorageEnabled = true
        binding.webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        binding.webView.settings.setGeolocationEnabled(true)
        binding.webView.settings.databaseEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                binding.loadingAnim.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)
                try {
                    binding.loadingAnim.visibility = View.GONE
                }catch (ex:Exception) {

                }
            }

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                try {
                    val builder = AlertDialog.Builder(requireContext())
                    var message = "SSL Certificate error."
                    val primaryError = error.primaryError
                    if (primaryError == 0) {
                        message = "The certificate is not yet valid."
                    } else if (primaryError == 1) {
                        message = "The certificate has expired."
                    } else if (primaryError == 2) {
                        message = "The certificate Hostname mismatch."
                    } else if (primaryError == 3) {
                        message = "The certificate authority is not trusted."
                    }
                    builder.setTitle("SSL Certificate Error")
                    builder.setMessage("$message Do you want to continue anyway?")
                    builder.setPositiveButton(
                        "continue"
                    ) { dialog, which -> handler.proceed() }
                    builder.setNegativeButton(
                        "cancel"
                    ) { dialog, which -> handler.cancel() }
                    builder.setCancelable(false)
                    builder.create().show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        val playVideo =
            "<html><body><iframe width=100%; height=100%; src=\"${videoLink}\" frameborder=\"0\" allowfullscreen></iframe></body></html>"
        binding.webView.loadData(playVideo, "text/html", "utf-8")
    }
    companion object {
        @JvmStatic
        fun newInstance(_videoLink: String) =
            EmbeddVideoLinkPlayDialog().apply {
                arguments = Bundle().apply {
                    videoLink = _videoLink
                }
            }
    }

}