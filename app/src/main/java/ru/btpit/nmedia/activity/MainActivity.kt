package ru.btpit.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.launch
import androidx.activity.result.registerForActivityResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.btpit.nmedia.R
import ru.btpit.nmedia.adapter.OnInteractionListener
import ru.btpit.nmedia.adapter.PostsAdapter
import ru.btpit.nmedia.databinding.ActivityMainBinding
import ru.btpit.nmedia.util.AndroidUtils
import ru.btpit.nmedia.dto.Post
import ru.btpit.nmedia.viewmodel.PostViewModel
import java.math.RoundingMode
import java.text.DecimalFormat


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: PostViewModel by viewModels()

        val newPostLauncher = registerForActivityResult(NewPostResultContract()) { result ->
            result ?: return@registerForActivityResult
            viewModel.changeContent(result)
            viewModel.save()
        }

        val editPostActivityContract =
            registerForActivityResult(EditPost()) { result ->
                result?: return@registerForActivityResult
                viewModel.changeContent(result)
                viewModel.save()
            }

        val adapter = PostsAdapter(object : OnInteractionListener{
            override fun onEdit(post: Post) {
                viewModel.edit(post)
                editPostActivityContract.launch(post.content)
            }

            override fun onVideo(post: Post) {
                val intentVideo = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                startActivity(intentVideo)
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }


            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                    viewModel.shareById(post.id)
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

        })
        binding.list?.adapter = adapter
        viewModel.data.observe(this) { posts ->
            adapter.submitList(posts)
        }
        binding.fab?.setOnClickListener {
            newPostLauncher.launch()
        }
    }
}



