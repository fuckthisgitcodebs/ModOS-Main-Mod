package com.mod.os.recents.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertClip(clip: ClipEntry): Long

    @Query("SELECT * FROM clips WHERE hash = :hash LIMIT 1")
    suspend fun findByHash(hash: String): ClipEntry?

    @Query("SELECT * FROM clips WHERE isArchived = 0 AND sourcePackage = :packageName ORDER BY timestamp DESC")
    fun observeActiveClipsForPackage(packageName: String): Flow<List<ClipEntry>>

    @Query("SELECT * FROM clips WHERE isArchived = 0 ORDER BY timestamp DESC LIMIT :limit")
    fun getActiveClips(limit: Int): Flow<List<ClipEntry>>

    @Query("SELECT * FROM clips WHERE isArchived = 1 ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getArchivedClipsPaginated(limit: Int, offset: Int): Flow<List<ClipEntry>>

    @Query("""
        SELECT clips.* FROM clips
        JOIN clips_fts ON clips.id = clips_fts.docid
        WHERE clips_fts MATCH :query
          AND clips.isArchived = 1
        ORDER BY clips.timestamp DESC
        LIMIT :limit OFFSET :offset
    """)
    fun searchArchivedPaginated(query: String, limit: Int, offset: Int): Flow<List<ClipEntry>>

    @Query("DELETE FROM clips WHERE isArchived = 0 AND id NOT IN " +
           "(SELECT id FROM clips WHERE isArchived = 0 ORDER BY timestamp DESC LIMIT :keepCount)")
    suspend fun trimActiveTo(keepCount: Int)

    @Query("UPDATE clips SET isArchived = 1 " +
           "WHERE sourcePackage IN (:powerApps) " +
           "AND timestamp < :threshold " +
           "AND id NOT IN (SELECT id FROM clips WHERE isArchived = 0 AND sourcePackage IN (:powerApps) LIMIT 30)")
    suspend fun archiveExcessPowerAppClips(powerApps: Set<String>, threshold: Instant)

    @Query("DELETE FROM clips WHERE isArchived = 1 AND sourcePackage NOT IN (:powerApps)")
    suspend fun deleteNonPowerAppArchive(powerApps: Set<String>)

    @Query("DELETE FROM clips WHERE id = :id")
    suspend fun deleteClip(id: Long)

    @Query("SELECT COUNT(*) FROM clips WHERE isArchived = 0")
    suspend fun countActiveClips(): Int
}
