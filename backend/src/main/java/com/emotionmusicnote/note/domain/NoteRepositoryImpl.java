package com.emotionmusicnote.note.domain;

import static com.emotionmusicnote.note.domain.QNote.note;

import com.emotionmusicnote.common.PageRequest;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NoteRepositoryImpl implements NoteCustomRepository {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public List<Note> findAll(Long userLoginId, PageRequest pageRequest) {
    System.out.println("userLoginId = " + userLoginId);

    return jpaQueryFactory.selectFrom(note)
        .join(note.user).fetchJoin()
        .where(note.user.id.eq(userLoginId))
        .orderBy(note.id.desc())
        .offset(pageRequest.getOffset())
        .limit(pageRequest.getSize())
        .fetch();
  }
}
