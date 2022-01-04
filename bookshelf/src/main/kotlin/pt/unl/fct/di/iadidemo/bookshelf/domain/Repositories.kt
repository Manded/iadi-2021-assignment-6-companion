/**
Copyright 2021 João Costa Seco

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package pt.unl.fct.di.iadidemo.bookshelf.domain

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*
import javax.transaction.Transactional

interface UserRepository : PagingAndSortingRepository<UserDAO, String>

interface BookRepository : CrudRepository<BookDAO, Long>

interface RoleRepository : CrudRepository<RoleDAO, String>

interface AuthorRepository : CrudRepository<AuthorDAO, Long>

interface ImageRepository : CrudRepository<ImageDAO, Long>

interface TokenRepository : CrudRepository<TokenDAO, String> {

    @Query("select t from UserDAO as u inner join u.tokens as t where u.name = :username and t.token = :token")
    fun getTokenByUser(username: String,
                       token: String): Optional<TokenDAO>

    @Transactional
    @Modifying
    @Query("delete from TokenDAO t where t.owner.username=:username")
    fun deleteTokensByUser(username: String)

}