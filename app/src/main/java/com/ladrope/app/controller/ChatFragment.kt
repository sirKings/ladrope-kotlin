package com.ladrope.app.controller


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.ladrope.app.Model.News
import com.ladrope.app.R
import com.squareup.picasso.Picasso


/**
 * A simple [Fragment] subclass.
 */
class ChatFragment : Fragment() {

    var adapter: NewsAdapter? = null
    var layoutManager: RecyclerView.LayoutManager? = null
    var options: FirebaseRecyclerOptions<News>? = null
    var newsRV: RecyclerView? = null
    var mProgressBar: ProgressBar? =null
    var mErrorText: TextView? = null
    //var mWebView: WebView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        mProgressBar = view.findViewById(R.id.chatProgressBar)
        mErrorText = view.findViewById(R.id.chatErrorText)
        newsRV = view.findViewById(R.id.chatRecyclerView)
        val messageBtn = view.findViewById<FloatingActionButton>(R.id.chatBtn)

        val database = FirebaseDatabase.getInstance()
        val query = database.getReference("blog")


        options = FirebaseRecyclerOptions.Builder<News>()
                .setQuery(query, News::class.java)
                .build()


        adapter = NewsAdapter(options!!, context!!)
        layoutManager = LinearLayoutManager(context)

        //set up recycler view
        newsRV!!.layoutManager = layoutManager
        newsRV!!.adapter = adapter

        messageBtn.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://m.me/ladrope")))
        }

        return view

    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }


    inner class NewsAdapter(options: FirebaseRecyclerOptions<News>, private val context: Context): FirebaseRecyclerAdapter<News, NewsAdapter.ViewHolder>(options){


        override fun onDataChanged() {
            super.onDataChanged()
            mProgressBar?.visibility = View.GONE
        }

        override fun onError(error: DatabaseError) {
            super.onError(error)
            mErrorText?.visibility = View.VISIBLE
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): NewsAdapter.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.news_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: NewsAdapter.ViewHolder, position: Int, model: News) {
            holder.bindItem(model)
        }

        inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            fun bindItem(news: News){
                val title = itemView.findViewById<TextView>(R.id.newsTitle)
                val description = itemView.findViewById<TextView>(R.id.newsStory)
                val imageView = itemView.findViewById<ImageView>(R.id.newsImage)
                val moreBtn = itemView.findViewById<TextView>(R.id.newsMore)

                title.text = news.title
                description.text = news.description

                Picasso.with(context).load(news.image).placeholder(R.drawable.ic_account_box_black_24dp).into(imageView)

                moreBtn.setOnClickListener {
                    val builder = CustomTabsIntent.Builder()
                    builder.setToolbarColor(resources.getColor(R.color.colorPrimary))
                    val customTabsIntent = builder.build()
                    customTabsIntent.intent.setPackage("com.android.chrome")
                    customTabsIntent.launchUrl(activity, Uri.parse(news.link))
                    Log.e("web", "called")
                }
            }
        }
    }

}// Required empty public constructor
