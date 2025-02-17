package pt.unl.fct.di.iadidemo.bookshelf.presentation.api.dto



data class BookDTO(val title:String, val authors:List<String>, val image:String)

data class BookListDTO(val id:Long, val title:String, val authors:List<AuthorsBookDTO>, val image: String)

data class AuthorsBookDTO(val name:String)



