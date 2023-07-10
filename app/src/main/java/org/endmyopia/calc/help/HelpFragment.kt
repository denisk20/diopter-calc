package org.endmyopia.calc.help

import android.content.res.AssetManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.noties.markwon.Markwon
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.file.FileSchemeHandler
import org.endmyopia.calc.R
import java.util.Scanner


/**
 * @author denisk
 * @since 9/17/20.
 */
class HelpFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_help, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // create markwon instance via builder method
        val markwon = Markwon.builder(requireContext())
            .usePlugin(ImagesPlugin.create { plugin ->
                plugin.addSchemeHandler(FileSchemeHandler.createWithAssets(requireContext()))
            })
            .build()
        val assetManager = requireContext().resources.assets
        val inputStream = assetManager.open("help.md", AssetManager.ACCESS_BUFFER)
        val md = Scanner(inputStream).useDelimiter("\\Z").next()

        val node = markwon.parse(md)
        val render = markwon.render(node)

        markwon.setParsedMarkdown(view.findViewById(R.id.help), render)
    }
}
