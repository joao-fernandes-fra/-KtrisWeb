package webktris.view

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLImageElement
import webktris.model.RenderState

class BackgroundRenderer(private val ctx: CanvasRenderingContext2D) {

    companion object {
        private const val PICSUM_BASE = "https://picsum.photos"
        private const val TRANSITION_DURATION = 2000.0
        private const val PAGE_SIZE = 50
        private const val TOTAL_PAGES = 20
    }

    private data class PicsumEntry(val id: String, val width: Int, val height: Int)

    private var pool = mutableListOf<PicsumEntry>()
    private var usedIds = mutableSetOf<String>()
    private var isFetching = false

    private var currentImage: HTMLImageElement? = null
    private var nextImage: HTMLImageElement? = null
    private var transitionStart = -10_000.0
    private var currentId: String = ""

    init {
        fetchPool()
    }

    private fun fetchPool(onReady: (() -> Unit)? = null) {
        if (isFetching) return
        isFetching = true

        val page = (1..TOTAL_PAGES).random()
        val url = "$PICSUM_BASE/v2/list?page=$page&limit=$PAGE_SIZE"

        window.fetch(url).then { response ->
            response.json()
        }.then { json ->
            @Suppress("UNCHECKED_CAST")
            val entries = (js("Array.from(json)") as Array<dynamic>).map { item ->
                PicsumEntry(
                    id = item.id.toString(),
                    width = (item.width as? Int) ?: 1920,
                    height = (item.height as? Int) ?: 1080
                )
            }
            pool.clear()
            usedIds.clear()
            pool.addAll(entries.shuffled())
            isFetching = false
            onReady?.invoke()
            if (currentImage == null) loadNext()
        }.catch {
            isFetching = false
        }
    }

    fun loadNext() {
        if (pool.none { it.id !in usedIds }) {
            fetchPool { loadNext() }
            return
        }

        val entry = pool.firstOrNull { it.id !in usedIds } ?: return
        usedIds.add(entry.id)
        loadImage(entry.id) { img ->
            if (currentImage == null) {
                currentImage = img
                currentId = entry.id
            } else {
                nextImage = img
                transitionStart = window.performance.now()
            }
        }
    }

    private fun loadImage(id: String, onLoad: (HTMLImageElement) -> Unit) {
        val img = document.createElement("img") as HTMLImageElement
        img.crossOrigin = "anonymous"
        img.src = "$PICSUM_BASE/id/$id/1920/1080"
        img.onload = { onLoad(img) }
        img.onerror = {
            usedIds.add(id)
            loadNext()
        } as ((Nothing, String, Int, Int, Any?) -> Any?)?
    }

    fun draw(canvasWidth: Int, canvasHeight: Int, state: RenderState) {
        val now = window.performance.now()
        val w = canvasWidth.toDouble()
        val h = canvasHeight.toDouble()

        ctx.save()

        currentImage?.let { drawImage(it, w, h) }

        if (nextImage != null) {
            val progress = ((now - transitionStart) / TRANSITION_DURATION).coerceIn(0.0, 1.0)
            ctx.globalAlpha = progress
            nextImage?.let { drawImage(it, w, h) }
            ctx.globalAlpha = 1.0

            if (progress >= 1.0) {
                currentImage = nextImage
                nextImage = null
            }
        }

        val overlay = if (state.isZoneActive) 0.77 else 0.62
        ctx.fillStyle = "rgba(0,0,0,$overlay)"
        ctx.fillRect(0.0, 0.0, w, h)

        if (state.isZoneActive) {
            ctx.fillStyle = "rgba(0,180,255,0.06)"
            ctx.fillRect(0.0, 0.0, w, h)
        }

        ctx.restore()
    }

    private fun drawImage(img: HTMLImageElement, w: Double, h: Double) {
        val scale = maxOf(w / img.width, h / img.height)
        val drawW = img.width * scale
        val drawH = img.height * scale
        ctx.filter = "blur(4px) brightness(0.85)"
        ctx.drawImage(img, (w - drawW) / 2, (h - drawH) / 2, drawW, drawH)
        ctx.filter = "none"
    }
}