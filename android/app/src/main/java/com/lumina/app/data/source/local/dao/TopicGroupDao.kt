// TopicGroupDao.kt
package com.lumina.app.data.source.local.dao

import androidx.room.*
import com.lumina.app.data.source.local.entity.TopicGroupEntity
import com.lumina.app.data.source.local.entity.TopicGroupWordEntity
import com.lumina.app.data.source.local.entity.VocabularyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicGroupDao {

    @Query("SELECT * FROM topic_groups WHERE unit_id = :unitId ORDER BY order_index ASC")
    fun getGroupsByUnit(unitId: Long): Flow<List<TopicGroupEntity>>

    @Query("""
        SELECT v.* FROM vocabulary v
        INNER JOIN topic_group_words tgw ON v.id = tgw.vocabulary_id
        WHERE tgw.topic_group_id = :groupId
    """)
    fun getVocabularyByGroup(groupId: Long): Flow<List<VocabularyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: TopicGroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<TopicGroupEntity>)

    @Update
    suspend fun updateGroup(group: TopicGroupEntity)

    @Delete
    suspend fun deleteGroup(group: TopicGroupEntity)

    // Junction table
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGroupWord(groupWord: TopicGroupWordEntity)

    @Delete
    suspend fun deleteGroupWord(groupWord: TopicGroupWordEntity)

    @Query("DELETE FROM topic_group_words WHERE topic_group_id = :groupId")
    suspend fun deleteAllWordsInGroup(groupId: Long)
}