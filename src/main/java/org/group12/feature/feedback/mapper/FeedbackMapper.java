package org.group12.feature.feedback.mapper;

import org.group12.feature.feedback.dto.response.FeedbackResponse;
import org.group12.feature.feedback.entity.Feedback;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {
    FeedbackResponse toFeedbackResponse(Feedback feedback);   // user -> UserInfo map theo tên field
}