package com.example.mrdetective.data.remote.api

import com.example.mrdetective.data.remote.dto.AccusationRequestDto
import com.example.mrdetective.data.remote.dto.AccusationResponseDto
import com.example.mrdetective.data.remote.dto.CameraChallengeDto
import com.example.mrdetective.data.remote.dto.CheckPuzzleDto
import com.example.mrdetective.data.remote.dto.ClueDto
import com.example.mrdetective.data.remote.dto.InvestigationDto
import com.example.mrdetective.data.remote.dto.InvestigationInsertDto
import com.example.mrdetective.data.remote.dto.NpcChatRequestDto
import com.example.mrdetective.data.remote.dto.MorseChallengeDto
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


interface OfflineApi {
    @POST("rest/v1/investigations?on_conflict=user_id,case_id")
    @Headers("Prefer: resolution=ignore-duplicates,return=representation")
    suspend fun startInvestigation(@Body body: InvestigationInsertDto): List<InvestigationDto>

    @GET("rest/v1/investigations?select=id,user_id,case_id,status,start_at")
    suspend fun getInvestigations(
        @Query("case_id") caseId: String?,
        @Query("user_id") userId: String,
    ): List<InvestigationDto>

    @GET("rest/v1/clues?select=*")
    suspend fun getCluesByCase(@Query("case_id") caseId: String): List<ClueDto>

    @GET("rest/v1/clues?select=id,image,title&image=not.is.null&order=id&limit=50")
    suspend fun getClueImages(@Query("id") excludeId: String): List<SceneClueImageDto>

    @GET("rest/v1/puzzles?select=id,case_id,kind,title,hint,reward_clue")
    suspend fun getPuzzlesByCase(@Query("case_id") caseId: String): List<PuzzleDto>

    @GET("rest/v1/puzzles?select=id,case_id,kind,title,hint,reward_clue")
    suspend fun getPuzzle(@Query("id") id: String): List<PuzzleDto>

    @GET("rest/v1/collected_clues?select=*")
    suspend fun getCollectedClues(@Query("investigation_id") investigationId: String): List<Map<String, Any?>>

    @GET("rest/v1/solved_puzzles?select=*")
    suspend fun getSolvedPuzzles(@Query("investigation_id") investigationId: String): List<Map<String, Any?>>

    @POST("rest/v1/collected_clues")
    @Headers("Prefer: resolution=ignore-duplicates,return=minimal")
    suspend fun collectClue(@Body body: Map<String, Int>)

    @POST("rest/v1/rpc/check_puzzle")
    suspend fun checkPuzzle(@Body body: CheckPuzzleDto): Boolean

    @POST("rest/v1/rpc/get_morse_challenges")
    suspend fun getMorseChallenges(@Body body: Map<String, Int>): List<MorseChallengeDto>

    @POST("rest/v1/rpc/get_camera_challenges")
    suspend fun getCameraChallenges(@Body body: Map<String, Int>): List<CameraChallengeDto>

    @POST("rest/v1/rpc/complete_camera_puzzle")
    suspend fun completeCameraPuzzle(@Body body: Map<String, Int>): Int

    @POST("rest/v1/rpc/complete_scene_camera_puzzle")
    suspend fun completeSceneCameraPuzzle(@Body body: Map<String, Int>): Int

    @POST("functions/v1/npc-chat")
    suspend fun npcChat(@Body body: NpcChatRequestDto): NpcChatResponseDto

    @GET("rest/v1/dialogue_messages?select=*")
    suspend fun getDialogue(@Query("investigation_id") investigationId: String, @Query("suspect_id") suspectId: String): List<Map<String, Any?>>

    @POST("functions/v1/submit-accusation")
    suspend fun submitAccusation(@Body body: AccusationRequestDto): AccusationResponseDto

    @GET("functions/v1/submit-accusation")
    suspend fun getAccusation(@Query("investigation_id") investigationId: Int): AccusationResponseDto

    @POST suspend fun postQueued(@Url url: String, @Header("Prefer") prefer: String?, @Body body: RequestBody): ResponseBody
    @DELETE
    suspend fun deleteQueued(@Url url: String): ResponseBody

    @POST("storage/v1/object/{bucket}/{path}")
    @Headers("x-upsert: true")
    suspend fun uploadPhoto(@Path("bucket") bucket: String, @Path(value = "path", encoded = true) path: String, @Body body: RequestBody): ResponseBody
}
