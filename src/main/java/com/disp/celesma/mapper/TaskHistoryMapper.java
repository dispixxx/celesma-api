package com.disp.celesma.mapper;

import com.disp.celesma.dto.task.history.TaskHistoryResponse;
import com.disp.celesma.model.TaskHistory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = UserMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TaskHistoryMapper {

    TaskHistoryResponse toResponse(TaskHistory history);
}
