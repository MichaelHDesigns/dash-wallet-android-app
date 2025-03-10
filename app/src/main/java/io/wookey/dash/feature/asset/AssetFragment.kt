package io.wookey.dash.feature.asset

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.wookey.dash.R
import io.wookey.dash.base.BaseFragment
import io.wookey.dash.core.ExchangeRatesHelper
import io.wookey.dash.core.WalletHelper
import io.wookey.dash.data.AppDatabase
import io.wookey.dash.data.entity.Asset
import io.wookey.dash.feature.wallet.WalletManagerActivity
import io.wookey.dash.support.BackgroundHelper
import io.wookey.dash.support.extensions.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.base_title_second.*
import kotlinx.android.synthetic.main.fragment_asset.*
import kotlinx.android.synthetic.main.item_asset.*

class AssetFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_asset, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val viewModel = ViewModelProviders.of(this).get(AssetViewModel::class.java)

        val appCompatActivity = activity as AppCompatActivity?
        appCompatActivity?.setSupportActionBar(toolbar)
        appCompatActivity?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(false)
        }
        with(leftIcon) {
            visibility = View.VISIBLE
            setImageResource(R.drawable.icon_home)
            setOnClickListener {
                startActivity(Intent(context, WalletManagerActivity::class.java))
            }
        }

        with(rightIcon) {
            visibility = View.VISIBLE
            setImageResource(R.drawable.icon_visible)
            setOnClickListener {
                viewModel.assetVisibleChanged()
            }
        }

        walletBg.setRateHeight(686 / 210f, screenWidth() - dp2px(16 * 2))
        dot.setImageDrawable(BackgroundHelper.getDotDrawable(context, R.color.color_59C698, dp2px(9)))

        copy.background = BackgroundHelper.getStokeBackground(context, R.color.color_FFFFFF, 1, dp2px(11))
        copy.setOnClickListener { copy(address.text.toString()) }

        BackgroundHelper.setItemShadowBackground(assetBg)

        recyclerView.layoutManager = LinearLayoutManager(context)

        val list = mutableListOf<Asset>()
        val adapter = AssetAdapter(list) {
            viewModel.onItemClick(it)
        }
        recyclerView.adapter = adapter

        viewModel.openAssetDetail.observe(this, Observer { value ->
            value?.let {
                startActivity(it.apply {
                    setClass(context, AssetDetailActivity::class.java)
                })
            }
        })

        viewModel.assetVisible.observe(this, Observer {
            rightIcon.setImageResource(R.drawable.icon_visible)
            adapter.notifyVisibilityChanged(true)
        })

        viewModel.assetInvisible.observe(this, Observer {
            rightIcon.setImageResource(R.drawable.icon_invisible)
            adapter.notifyVisibilityChanged(false)
        })

        viewModel.initVisible()

        AppDatabase.getInstance().walletDao().loadActiveWallet().observe(this, Observer { wallet ->
            wallet?.let {
                centerTitle.text = "Butk"
                walletName.text = it.name
                address.text = it.address

                AppDatabase.getInstance().assetDao().loadAssetsByWalletId(it.id).observe(this, Observer { value ->
                    value?.let { assets ->
                        list.clear()
                        list.addAll(assets)
                        adapter.notifyDataSetChanged()
                    }
                })
            }
        })

        WalletHelper.instance.unSignedBalance.observe(this, Observer { value ->
            value?.let {
                viewModel.updateBalance(it)
            }
        })
        WalletHelper.instance.transactions.observe(this, Observer { value ->
            value?.let {
                viewModel.handleTransactions(it)
            }
        })
        viewModel.initData()
        ExchangeRatesHelper.instance.rate.observe(this, Observer {
            adapter.notifyDataSetChanged()
        })
    }

    class AssetAdapter(val data: List<Asset>, private val listener: (Asset) -> Unit) :
            RecyclerView.Adapter<AssetAdapter.ViewHolder>() {

        private var visibility = true

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_asset, parent, false)
            return ViewHolder(view, listener)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindViewHolder(data[position], visibility)
        }

        fun notifyVisibilityChanged(value: Boolean) {
            visibility = value
            notifyDataSetChanged()
        }

        class ViewHolder(override val containerView: View, private val listener: (Asset) -> Unit) :
                RecyclerView.ViewHolder(containerView), LayoutContainer {

            fun bindViewHolder(asset: Asset, visibility: Boolean) {
                with(asset) {
                    icon.setImage(token)
                    title.text = "Butk"
                    if (visibility) {
                        subTitle.text = balance.formatterAmountStrip()
                        hint.text = balance.formatRate()
                    } else {
                        subTitle.text = "******"
                        hint.text = "******"
                    }
                    itemView.setOnClickListener {
                        listener(this)
                    }
                }
            }
        }
    }
}