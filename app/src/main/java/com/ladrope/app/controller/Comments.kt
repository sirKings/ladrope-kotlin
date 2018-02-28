package com.ladrope.app.controller

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ladrope.app.Model.Cloth
import com.ladrope.app.Model.Comment
import com.ladrope.app.Model.User
import com.ladrope.app.R
import com.ladrope.app.Utilities.GENDER
import com.ladrope.app.Utilities.ReportCommentTask
import kotlinx.android.synthetic.main.activity_comments.*

class Comments : AppCompatActivity() {

    var adapter: CommentsAdapter? = null
    var layoutManager: RecyclerView.LayoutManager? = null
    var options: FirebaseRecyclerOptions<Comment>? = null
    var commentRV: RecyclerView? = null
    var mProgressBar: ProgressBar? =null
    var mErrorText: TextView? = null
    var clothKey: String? = null
    var uid: String? = null
    var cloth: Cloth? = null
    var mUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        clothKey = intent.extras.get("clothKey") as String
        mProgressBar = commentProgressBar
        mErrorText = commentErrorText
        commentRV = commentRecyclerView
        uid = FirebaseAuth.getInstance().uid
        getCloth()
        getUser()

        val database = FirebaseDatabase.getInstance()
        val query = database.getReference("cloths").child(GENDER).child(clothKey).child("comment")


        options = FirebaseRecyclerOptions.Builder<Comment>()
                .setQuery(query, Comment::class.java)
                .build()


        adapter = CommentsAdapter(options!!, this)
        layoutManager = LinearLayoutManager(this)

        //set up recycler view
        commentRV!!.layoutManager = layoutManager
        commentRV!!.adapter = adapter


    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    fun sendComment(view: View){
        if (commentText.text.toString() == ""){

        }else{
            val comment = Comment()
            comment.title = mUser?.displayName
            comment.message = commentText.text.toString()
            val commentRef = FirebaseDatabase.getInstance().reference.child("cloths").child(GENDER).child(clothKey).child("comment").push()
            commentRef.setValue(comment).addOnCompleteListener {
                val numCommentRef = FirebaseDatabase.getInstance().reference.child("cloths").child(GENDER).child(clothKey).child("numComment")
                val num = cloth?.numComment!! + 1
                numCommentRef.setValue(num).addOnCompleteListener {
                    ReportCommentTask(clothKey!!, uid!!, comment.message!!, this).execute()
                }
            }
        }

        commentText.setText("")
    }


    inner class CommentsAdapter(options: FirebaseRecyclerOptions<Comment>, private val context: Context): FirebaseRecyclerAdapter<Comment, CommentsAdapter.ViewHolder>(options){

        override fun onDataChanged() {
            super.onDataChanged()
            mProgressBar?.visibility = View.GONE
        }

        override fun onError(error: DatabaseError) {
            super.onError(error)
            mProgressBar?.visibility = View.GONE
            commentSendBtn?.isClickable = false
            mErrorText?.visibility = View.VISIBLE
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CommentsAdapter.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.comment_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: CommentsAdapter.ViewHolder, position: Int, model: Comment) {
            holder.bindItem(model)
        }

        inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            fun bindItem(comment: Comment){
                val title = itemView.findViewById<TextView>(R.id.commentTitle)
                val message = itemView.findViewById<TextView>(R.id.commentMessage)

                title.text = comment.title
                message.text = comment.message

            }
        }
    }

    fun getCloth(){
        val clothRef = FirebaseDatabase.getInstance().reference.child("cloths").child(GENDER).child(clothKey)
        clothRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot?) {
                if (p0!!.exists()){
                    cloth = p0.getValue(Cloth::class.java)
                    Log.e("cloth name", cloth?.label)
                }
            }

            override fun onCancelled(p0: DatabaseError?) {

            }
        })
    }

    fun getUser(){
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot?) {
                mUser = p0?.getValue(User::class.java)
            }

            override fun onCancelled(p0: DatabaseError?) {

            }
        })
    }
}
