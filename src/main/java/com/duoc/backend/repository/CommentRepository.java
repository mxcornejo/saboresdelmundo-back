package com.duoc.backend.repository;

import com.duoc.backend.entity.Comment;
import com.duoc.backend.entity.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByRecetaIdAndEstado(Long recetaId, CommentStatus estado);

    List<Comment> findByEstado(CommentStatus estado);

    List<Comment> findByAutorId(Long autorId);

    List<Comment> findByRecetaId(Long recetaId);
}
