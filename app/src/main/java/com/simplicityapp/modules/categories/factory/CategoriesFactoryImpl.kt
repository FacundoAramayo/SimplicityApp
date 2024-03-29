package com.simplicityapp.modules.categories.factory

import com.simplicityapp.modules.categories.model.Category

class CategoriesFactoryImpl: CategoriesFactory {

    override fun getCategoriesList(): List<Category> {
        return listOf(
            Category(1, "PRUEBA1", "https://homepages.cae.wisc.edu/~ece533/images/boat.png"),
            Category(2, "PRUEBA2", "https://i.picsum.photos/id/381/200/300.jpg"),
            Category(3, "PRUEBA3", "https://i.picsum.photos/id/400/200/300.jpg"),
            Category(4, "PRUEBA4", "https://i.picsum.photos/id/500/200/300.jpg")
        )
    }
}