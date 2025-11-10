package com.tagaev.data.models

sealed class Resource<out R> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error<T>(val exception: Exception? = null, val causes: String? = null) : Resource<T>()
    object Loading : Resource<Nothing>()
}