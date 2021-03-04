package com.syftapp.codetest.posts

import com.syftapp.codetest.data.model.domain.Post
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent

class PostsPresenter(
    private val getPostsUseCase: GetPostsUseCase,
    private val deletePostUseCase: DeletePostUseCase
) : KoinComponent {

    private var currentState: PostScreenState? = null

    private val compositeDisposable = CompositeDisposable()
    private lateinit var view: PostsView

    fun bind(view: PostsView) {
        this.view = view
        compositeDisposable.add(loadPosts())
    }

    fun unbind() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }

    fun showDetails(post: Post) {
        view.render(PostScreenState.PostSelected(post))
    }

    fun deletePost(post: Post) {
        deletePostUseCase.execute(post).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { view.render(PostScreenState.Loading) }
            .doAfterTerminate { view.render(PostScreenState.FinishedLoading) }
            .subscribe(
                { view.render(PostScreenState.DataAvailable(it)) },
                { view.render(PostScreenState.Error(it)) })
    }

    fun loadPosts() = getPostsUseCase.execute()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { view.render(PostScreenState.Loading) }
        .doAfterTerminate { view.render(PostScreenState.FinishedLoading) }
        .subscribe(
            { view.render(PostScreenState.DataAvailable(it)) },
            { view.render(PostScreenState.Error(it)) }
        )

    fun loadNextPage() {
        //wait to load next page, make sure that not busy loading
        if (currentState == PostScreenState.Loading) return
        loadPosts()
    }

    fun setState(state: PostScreenState) {
        currentState = state
    }
}