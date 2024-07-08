package com.rizzi.bouquet.loader

interface ExecutionListener {
    fun onStart()
    fun onLoading(progress: Float)
    fun onSuccess(result: DocumentResult.Success) {}
    fun onCancel() {}
    fun onFailure(result: DocumentResult.Error) {}
}