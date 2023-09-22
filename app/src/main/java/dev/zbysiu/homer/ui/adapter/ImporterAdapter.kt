package dev.zbysiu.homer.ui.adapter

import android.content.Context
import android.view.ViewGroup
import dev.zbysiu.homer.databinding.ItemImporterBinding
import dev.zbysiu.homer.importer.Importer
import dev.zbysiu.homer.util.Stability
import dev.zbysiu.homer.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter

class ImporterAdapter(
    context: Context,
    items: Array<Importer>,
    private val onImportClickedListener: ((Importer) -> Unit)
) : BaseAdapter<Importer>(context) {

    init {
        data.addAll(items.toList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<Importer> {
        return ImporterViewHolder(ItemImporterBinding.inflate(inflater, parent, false))
    }

    inner class ImporterViewHolder(
        private val vb: ItemImporterBinding
    ) : BaseAdapter.ViewHolder<Importer>(vb.root) {

        override fun bindToView(content: Importer, position: Int) {
            with(content) {
                vb.ivItemImport.setImageResource(icon)
                vb.tvItemImportTitle.setText(title)
                vb.tvItemImportDescription.setText(description)

                vb.btnItemImport.setOnClickListener {
                    onImportClickedListener(this)
                }

                vb.tvItemImportBeta.setVisible(stability == Stability.BETA)
            }
        }
    }
}