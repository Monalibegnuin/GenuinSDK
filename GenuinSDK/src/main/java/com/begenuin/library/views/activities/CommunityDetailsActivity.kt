package com.begenuin.library.views.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.addCallback
import com.begenuin.library.R
import com.begenuin.library.databinding.ActivityCommunityDetailsBinding
import com.begenuin.library.views.fragments.CommunityDetailsFragment

class CommunityDetailsActivity : AppCompatActivity() {

   private lateinit var communityDetailBinding: ActivityCommunityDetailsBinding
    private lateinit var communityDetailsFragment: CommunityDetailsFragment
    var communityId = ""
    private var role: Int = 0

    companion object{
        lateinit var activity: CommunityDetailsActivity
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        communityDetailBinding = ActivityCommunityDetailsBinding.inflate(layoutInflater)
        setContentView(communityDetailBinding.root)
        activity = this
        getDataFromIntent()

        communityDetailsFragment = CommunityDetailsFragment.newInstance(communityId, role)
        supportFragmentManager.beginTransaction()
            .add(R.id.communityDetails, communityDetailsFragment)
            .addToBackStack("CommunityDetails").commit()

        onBackPressedDispatcher.addCallback(this) {
            backManage()
        }

    }


    private fun getDataFromIntent() {
        if (intent != null && intent.extras != null) {
            communityId = intent.extras!!.getString("community_id", "")
            role = intent.extras!!.getInt("role", 0)
        }
    }

    fun backManage() {
        if (communityDetailsFragment.childFragmentManager.backStackEntryCount > 0) {
            communityDetailsFragment.manageBackPress()
        }
    }
}