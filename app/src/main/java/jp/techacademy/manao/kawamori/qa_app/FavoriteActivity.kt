package jp.techacademy.manao.kawamori.qa_app

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.util.Base64  //追加する
import android.widget.ListView
import com.google.firebase.database.*

class FavoriteActivity : AppCompatActivity() {

    private var mGenre = 0

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        // UIの準備
        title = "お気に入り"

        // ListViewの準備
        mListView = findViewById(R.id.listView)
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        mListView.setOnItemClickListener { parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        // ListViewの準備
        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        mListView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        // Firebase
        val user = FirebaseAuth.getInstance().currentUser
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        mDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(ds in dataSnapshot.child(FavoritesPATH).child(user!!.uid).children){
                    val mGQuestion = mDatabaseReference.child(ContentsPATH).child(ds.value.toString()).child(ds.key.toString())
                    mGQuestion.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val map = dataSnapshot.value as Map<String, String>
                            val title = map["title"] ?: ""
                            val body = map["body"] ?: ""
                            val name = map["name"] ?: ""
                            val uid = map["uid"] ?: ""
                            val imageString = map["image"] ?: ""
                            val bytes =
                                if (imageString.isNotEmpty()) {
                                    Base64.decode(imageString, Base64.DEFAULT)
                                } else {
                                    byteArrayOf()
                                }

                            val answerArrayList = ArrayList<Answer>()
                            val answerMap = map["answers"] as Map<String, String>?
                            if (answerMap != null) {
                                for (key in answerMap.keys) {
                                    val temp = answerMap[key] as Map<String, String>
                                    val answerBody = temp["body"] ?: ""
                                    val answerName = temp["name"] ?: ""
                                    val answerUid = temp["uid"] ?: ""
                                    val answer = Answer(answerBody, answerName, answerUid, key)
                                    answerArrayList.add(answer)
                                }
                            }
                            val question = Question(title, body, name, uid, dataSnapshot.key ?: "",
                                mGenre, bytes, answerArrayList)
                            mQuestionArrayList.add(question)
                            mAdapter.notifyDataSetChanged()
                        }
                        override fun onCancelled(firebaseError: DatabaseError) {

                        }
                    })

                }
                mAdapter.setQuestionArrayList(mQuestionArrayList)
                mListView.adapter = mAdapter
                mAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(firebaseError: DatabaseError) {

            }
        })
    }

}