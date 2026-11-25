package com.example.mrdetective.data.remote.api

import com.example.mrdetective.data.remote.dto.AccusationRequestDto
import com.example.mrdetective.data.remote.dto.AccusationResponseDto
import com.example.mrdetective.data.remote.dto.CameraChallengeDto
import com.example.mrdetective.data.remote.dto.CheckPuzzleDto
import com.example.mrdetective.data.remote.dto.ClueDto
import com.example.mrdetective.data.remote.dto.InvestigationDto
import com.example.mrdetective.data.remote.dto.InvestigationInsertDto
import com.example.mrdetective.data.remote.dto.MorseChallengeDto
import com.example.mrdetective.data.remote.dto.NpcChatRequestDto
import com.example.mrdetective.data.remote.dto.NpcChatResponseDto
import com.example.mrdetective.data.remote.dto.PuzzleDto
import com.example.mrdetective.data.remote.dto.SceneClueImageDto
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/** Retrofit contract for investigation progress, puzzles, dialogue, and queued operations. */
interface OfflineApi {
    /** Creates or updates the player's investigation on Supabase. */
    @POST("rest/v1/investigations?on_conflict=user_id,case_id")
    @Headers("Prefer: resolution=ignore-duplicates,return=representation")
    suspend fun startInvestigation(
        @Body body: InvestigationInsertDto,
    ): List<InvestigationDto>

    /** Reads investigation rows matching the supplied filters. */
    @GET("rest/v1/investigations?select=id,user_id,case_id,status,start_at")
    suspend fun getInvestigations(
        @Query("case_id") caseId: String?,
        @Query("user_id") userId: String,
    ): List<InvestigationDto>

    /** Reads clue metadata for the specified case. */
    @GET("rest/v1/clues?select=*")
    suspend fun getCluesByCase(
        @Query("case_id") caseId: String,
    ): List<ClueDto>

    /** Reads public clue image metadata for scene search. */
    @GET("rest/v1/clues?select=id,image,title&image=not.is.null&order=id&limit=50")
    suspend fun getClueImages(
        @Query("id") excludeId: String,
    ): List<SceneClueImageDto>

    /** Reads the public puzzle metadata for the specified case. */
    @GET("rest/v1/puzzles?select=id,case_id,kind,title,hint,reward_clue")
    suspend fun getPuzzlesByCase(
        @Query("case_id") caseId: String,
    ): List<PuzzleDto>

    /** Reads public metadata for the selected puzzle. */
    @GET("rest/v1/puzzles?select=id,case_id,kind,title,hint,reward_clue")
    suspend fun getPuzzle(
        @Query("id") id: String,
    ): List<PuzzleDto>

    /** Reads the clue links recorded for an investigation. */
    @GET("rest/v1/collected_clues?select=*")
    suspend fun getCollectedClues(
        @Query("investigation_id") investigationId: String,
    ): List<Map<String, Any?>>

    /** Reads the puzzle completions recorded for an investigation. */
    @GET("rest/v1/solved_puzzles?select=*")
    suspend fun getSolvedPuzzles(
        @Query("investigation_id") investigationId: String,
    ): List<Map<String, Any?>>

    /** Writes the association between an investigation and a collected clue. */
    @POST("rest/v1/collected_clues")
    @Headers("Prefer: resolution=ignore-duplicates,return=minimal")
    suspend fun collectClue(
        @Body body: Map<String, Int>,
    )

    /** Calls the server-side answer validator for the selected puzzle. */
    @POST("rest/v1/rpc/check_puzzle")
    suspend fun checkPuzzle(
        @Body body: CheckPuzzleDto,
    ): Boolean

    /** Fetches the Morse challenge records for the selected case. */
    @POST("rest/v1/rpc/get_morse_challenges")
    suspend fun getMorseChallenges(
        @Body body: Map<String, Int>,
    ): List<MorseChallengeDto>

    /** Fetches the camera challenge records for the selected case. */
    @POST("rest/v1/rpc/get_camera_challenges")
    suspend fun getCameraChallenges(
        @Body body: Map<String, Int>,
    ): List<CameraChallengeDto>

    /** Submits a camera puzzle proof to the completion function. */
    @POST("rest/v1/rpc/complete_camera_puzzle")
    suspend fun completeCameraPuzzle(
        @Body body: Map<String, Int>,
    ): Int

    /** Submits a found scene item through the camera puzzle completion flow. */
    @POST("rest/v1/rpc/complete_scene_camera_puzzle")
    suspend fun completeSceneCameraPuzzle(
        @Body body: Map<String, Int>,
    ): Int

    /** Sends a question and optional evidence to the suspect chat function. */
    @POST("functions/v1/npc-chat")
    suspend fun npcChat(
        @Body body: NpcChatRequestDto,
    ): NpcChatResponseDto

    /** Retrieves stored messages for the specified investigation and suspect. */
    @GET("rest/v1/dialogue_messages?select=*")
    suspend fun getDialogue(
        @Query("investigation_id") investigationId: String,
        @Query("suspect_id") suspectId: String,
    ): List<Map<String, Any?>>

    /** Sends an accusation to the server for evaluation. */
    @POST("functions/v1/submit-accusation")
    suspend fun submitAccusation(
        @Body body: AccusationRequestDto,
    ): AccusationResponseDto

    /** Retrieves the verdict already recorded for an investigation. */
    @GET("functions/v1/submit-accusation")
    suspend fun getAccusation(
        @Query("investigation_id") investigationId: Int,
    ): AccusationResponseDto

    /** Sends a serialized queued write to its configured path. */
    @POST suspend fun postQueued(
        @Url url: String,
        @Header("Prefer") prefer: String?,
        @Body body: RequestBody,
    ): ResponseBody

    /** Sends a queued deletion to its configured path. */
    @DELETE
    suspend fun deleteQueued(
        @Url url: String,
    ): ResponseBody

    /** Uploads the locally captured photo to Supabase Storage. */
    @POST("storage/v1/object/{bucket}/{path}")
    @Headers("x-upsert: true")
    suspend fun uploadPhoto(
        @Path("bucket") bucket: String,
        @Path(value = "path", encoded = true) path: String,
        @Body body: RequestBody,
    ): ResponseBody
}
