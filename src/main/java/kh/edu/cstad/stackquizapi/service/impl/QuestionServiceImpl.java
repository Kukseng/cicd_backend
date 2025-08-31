package kh.edu.cstad.stackquizapi.service.impl;

import jakarta.transaction.Transactional;
import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.dto.request.CreateQuestionRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateQuestionRequest;
import kh.edu.cstad.stackquizapi.dto.response.QuestionResponse;
import kh.edu.cstad.stackquizapi.mapper.QuestionMapper;
import kh.edu.cstad.stackquizapi.repository.QuestionRepository;
import kh.edu.cstad.stackquizapi.repository.QuizRepository;
import kh.edu.cstad.stackquizapi.service.QuestionService;
import kh.edu.cstad.stackquizapi.util.QuestionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;

    private static final Set<QuestionType> VALID_QUESTION_TYPES =
            EnumSet.of(QuestionType.TF, QuestionType.MCQ, QuestionType.FILL_THE_BLANK);

    @Override
    public QuestionResponse createNewQuestion(CreateQuestionRequest createQuestionRequest) {

        Quiz quiz = quizRepository.findById(createQuestionRequest.quizId()).orElseThrow(
                ()-> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Quiz Id not Found" )
        );

        if (createQuestionRequest.type() == null || !VALID_QUESTION_TYPES.contains(createQuestionRequest.type())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid question type. Must be one of: " + VALID_QUESTION_TYPES);
        }

        try {
            Integer maxOrder = questionRepository.findMaxQuestionOrderByQuizId(createQuestionRequest.quizId());
            int nextOrder = (maxOrder != null) ? maxOrder + 1 : 1;

            Question question = questionMapper.fromCreateQuestionRequest(createQuestionRequest);
            question.setQuiz(quiz);
            question.setQuestionOrder(nextOrder);
            question.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            question.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

            String text = createQuestionRequest.text().toLowerCase().replace(' ', '_');
            question.setText(text);

            log.debug("Creating question with auto-generated order: {}", nextOrder);
            question = questionRepository.save(question);
            log.debug("Question created with ID: {}", question.getId());

            return questionMapper.toQuestionResponse(question);

        } catch (Exception exception) {
            log.error("Unexpected error while creating question", exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred while creating question");
        }
    }

    @Override
    public List<QuestionResponse> getAllQuestions() {

        try {

            List<Question> questions = questionRepository.findAll();

            if (questions.isEmpty()) {
                log.info("No questions found");
                return Collections.emptyList();
            }

            log.info("Found {} questions", questions.size());

            return questions.stream()
                    .map(questionMapper::toQuestionResponse)
                    .collect(Collectors.toList());

        } catch (Exception exception) {
            log.error("Unexpected error while fetching all questions", exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred while fetching questions");
        }
    }

    @Override
    public QuestionResponse getQuestionById(String id) {

        Question question = questionRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Question ID not found"
                        )
                );

        return questionMapper.toQuestionResponse(question);
    }

    @Transactional
    @Override
    public QuestionResponse updateQuestionById(String id, UpdateQuestionRequest updateQuestionRequest) {

        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Question id not found"
                ));

        try {
            questionMapper.toQuestionPartially(updateQuestionRequest, question);
            question.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
            Question updatedQuestion = questionRepository.save(question);

            log.info("Successfully updated question with ID: {}", id);

            return questionMapper.toQuestionResponse(updatedQuestion);

        } catch (Exception e) {
            log.error("Unexpected error while updating question with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to update question", e);
        }
    }

    @Override
    @Transactional
    public void deleteQuestionById(String id) {

        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Question ID cannot be null");
        }

        try {
            Question question = questionRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Question with ID " + id + " not found"));

            log.info("Deleting question '{}' with ID: {}", question.getText(), id);
            questionRepository.delete(question);
            log.info("Successfully deleted question with ID: {}", id);
        } catch (Exception exception) {
            log.error("Unexpected error while deleting question {}", id, exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred while deleting question");
        }
    }

//    @Transactional
//    @Override
//    public void disableQuestionById(String id) {
//
//        if (id == null || id.trim().isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                    "Question ID cannot be null or empty");
//        }
//
//        log.info("Attempting to disable question with ID: {}", id);
//
//        if (!questionRepository.existsById(id)) {
//            log.warn("Question not found for ID: {}", id);
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
//                    "Question id not found");
//        }
//
//        try {
//            questionRepository.disableById(id);
//            log.info("Successfully disabled question with ID: {}", id);
//
//        } catch (Exception e) {
//            log.error("Failed to disable question with ID: {}", id, e);
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
//                    "Failed to disable question", e);
//        }
//    }

    @Transactional
    public void deleteQuestionsByIds(List<String> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Question IDs list cannot be null or empty");
        }

        if (ids.size() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot delete more than 100 questions at once");
        }

        try {
            List<Question> questionsToDelete = questionRepository.findAllById(ids);

            if (questionsToDelete.size() != ids.size()) {
                Set<String> foundIds = questionsToDelete.stream()
                        .map(Question::getId)
                        .collect(Collectors.toSet());
                List<String> notFoundIds = ids.stream()
                        .filter(id -> !foundIds.contains(id))
                        .toList();

                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Questions not found with IDs: " + notFoundIds);
            }

            questionRepository.deleteAll(questionsToDelete);
            log.info("Successfully deleted {} questions", questionsToDelete.size());

        } catch (Exception exception) {
            log.error("Unexpected error while deleting questions", exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred while deleting questions");
        }
    }

}
