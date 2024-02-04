package com.emotionmusicnote.common;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import javax.annotation.processing.Generated;


/**
 * QBaseTime is a Querydsl query type for BaseTime
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QBaseTime extends EntityPathBase<BaseTime> {

  private static final long serialVersionUID = -2093507460L;

  public static final QBaseTime baseTime = new QBaseTime("baseTime");

  public final DatePath<java.time.LocalDate> createdDate = createDate("createdDate",
      java.time.LocalDate.class);

  public final DatePath<java.time.LocalDate> modifiedDate = createDate("modifiedDate",
      java.time.LocalDate.class);

  public QBaseTime(String variable) {
    super(BaseTime.class, forVariable(variable));
  }

  public QBaseTime(Path<? extends BaseTime> path) {
    super(path.getType(), path.getMetadata());
  }

  public QBaseTime(PathMetadata metadata) {
    super(BaseTime.class, metadata);
  }

}

