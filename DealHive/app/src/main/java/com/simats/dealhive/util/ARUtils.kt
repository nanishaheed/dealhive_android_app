package com.simats.dealhive.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import kotlin.collections.iterator

/**
 * AR Model data class containing URLs for different platforms
 */
data class ARModel(
    val glb: String  // Can be GLB or USDZ URL
)

/**
 * AR utility object for handling AR model lookup and viewing
 */
object ARUtils {
    
    // Product models mapped by keyword
    private val PRODUCT_MODELS = mapOf(
        "iphone" to ARModel(
            glb = "https://raw.githubusercontent.com/NiranjanKumar001/iPhone-16-Pro-Clone/main/public/models/scene.glb"
        ),
        "samsung" to ARModel(
            glb = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb"
        ),
        "galaxy" to ARModel(
            glb = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb"
        ),
        "s24" to ARModel(
            glb = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb"
        ),
        "oneplus" to ARModel(
            glb = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb"
        ),
        "tv" to ARModel(
            glb = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb"
        ),
        "macbook" to ARModel(
            glb = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb"
        ),
        "laptop" to ARModel(
            glb = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb"
        )
    )
    
    /**
     * Checks if a product has an AR model available based on its title or brand.
     * Returns the ARModel if found, null otherwise.
     */
    fun getARModelForProduct(title: String, brand: String? = null): ARModel? {
        val searchStr = "${brand.orEmpty()} $title".lowercase()
        
        // Check each product keyword
        for ((keyword, model) in PRODUCT_MODELS) {
            if (searchStr.contains(keyword)) {
                return model
            }
        }
        
        return null
    }
    
    /**
     * Opens AR viewer - supports both GLB and USDZ formats
     * For GLB: Uses Google Scene Viewer
     * For USDZ: Opens in a web-based model-viewer
     */
    fun openARViewer(context: Context, model: ARModel, title: String) {
        try {
            val modelUrl = model.glb
            
            // Check if it's a USDZ file
            if (modelUrl.lowercase().endsWith(".usdz")) {
                // Open USDZ in model-viewer web page
                openUsdzInWebViewer(context, modelUrl, title)
            } else {
                // Use Google Scene Viewer for GLB/GLTF
                openGlbInSceneViewer(context, modelUrl, title)
            }
        } catch (e: Exception) {
            Toast.makeText(
                context, 
                "Failed to open AR viewer: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    /**
     * Opens GLB file in Google Scene Viewer
     */
    private fun openGlbInSceneViewer(context: Context, modelUrl: String, title: String) {
        val sceneViewerUrl = "https://arvr.google.com/scene-viewer/1.0" +
            "?file=${Uri.encode(modelUrl)}" +
            "&mode=ar_only" +
            "&title=${Uri.encode(title)}"
        
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sceneViewerUrl))
        context.startActivity(intent)
    }
    
    /**
     * Opens USDZ file in a web-based 3D viewer using model-viewer
     * This works on Android by displaying in a Chrome Custom Tab
     */
    private fun openUsdzInWebViewer(context: Context, modelUrl: String, title: String) {
        // Create a simple HTML page with model-viewer that displays the 3D model
        // Using a data URI approach or a hosted viewer page
        
        // Option: Use a model-viewer hosted page (you can host this on your server)
        val viewerHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>${title}</title>
                <script type="module" src="https://ajax.googleapis.com/ajax/libs/model-viewer/3.3.0/model-viewer.min.js"></script>
                <style>
                    body { margin: 0; padding: 0; background: #1a1a1a; }
                    model-viewer {
                        width: 100vw;
                        height: 100vh;
                        --poster-color: #1a1a1a;
                    }
                    .title {
                        position: absolute;
                        top: 20px;
                        left: 20px;
                        color: white;
                        font-family: -apple-system, BlinkMacSystemFont, sans-serif;
                        font-size: 18px;
                        font-weight: bold;
                        text-shadow: 0 2px 4px rgba(0,0,0,0.5);
                    }
                </style>
            </head>
            <body>
                <div class="title">${title}</div>
                <model-viewer
                    src="${modelUrl}"
                    ios-src="${modelUrl}"
                    alt="${title}"
                    ar
                    ar-modes="webxr scene-viewer quick-look"
                    camera-controls
                    touch-action="pan-y"
                    auto-rotate
                    shadow-intensity="1"
                    exposure="0.8"
                ></model-viewer>
            </body>
            </html>
        """.trimIndent()
        
        // Encode HTML for data URI
        val encodedHtml = Base64.encodeToString(
            viewerHtml.toByteArray(Charsets.UTF_8),
            Base64.NO_PADDING
        )
        
        val dataUri = "data:text/html;base64,$encodedHtml"
        
        // Open in Chrome Custom Tab for better experience
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, Uri.parse(dataUri))
        } catch (e: Exception) {
            // Fallback to regular browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(dataUri))
            context.startActivity(intent)
        }
    }
    
    /**
     * Checks if a product has AR support
     */
    fun hasARSupport(title: String, brand: String? = null): Boolean {
        return getARModelForProduct(title, brand) != null
    }
}
