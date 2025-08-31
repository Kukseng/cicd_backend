package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.CreateQuestionRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateQuestionRequest;
import kh.edu.cstad.stackquizapi.dto.response.QuestionResponse;
import kh.edu.cstad.stackquizapi.util.QuestionType;

import java.util.List;

/**
 * Service interface for managing questions in the system.
 *
 * @author Prch Rattanakmony
 */
public interface QuestionService {

    /**
     * Creates a new question in the system.
     *
     * @param createQuestionRequest the request object containing question details
     * @return the created question as a {@link QuestionResponse}
     */
    QuestionResponse createNewQuestion(CreateQuestionRequest createQuestionRequest);

    /**
     * Retrieves all questions from the system.
     *
     * @return a list of {@link QuestionResponse} objects representing all questions
     */
    List<QuestionResponse> getAllQuestions();

    /**
     * Retrieves questions filtered by its id.
     *
     * @param id the {@link QuestionType} to filter by
     * @return a list of {@link QuestionResponse} objects matching the given type
     */
    QuestionResponse getQuestionById(String id);

    /**
     * Updates an existing question by its ID.
     *
     * @param id the ID of the question to update
     * @param updateQuestionRequest the request object containing updated details
     * @return the updated question as a {@link QuestionResponse}
     */
    QuestionResponse updateQuestionById(String id, UpdateQuestionRequest updateQuestionRequest);

    /**
     * Deletes a question by its ID.
     *
     * @param id the ID of the question to delete
     */
    void deleteQuestionById(String id);

    /**
     * Deletes multiple questions by their IDs.
     *
     * @param ids a list of IDs of the questions to delete
     */
    void deleteQuestionsByIds(List<String> ids);

}

