package br.com.devsuperior.dev_xp_ai.repository;

import br.com.devsuperior.dev_xp_ai.entity.DeveloperEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeveloperRepository extends CrudRepository<DeveloperEntity, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByNicknameIgnoreCase(String nickname);

    @Query("SELECT * FROM tb_developer WHERE (:uf IS NULL OR uf = :uf) AND (:language IS NULL OR LOWER(primary_language) = LOWER(:language)) ORDER BY id")
    List<DeveloperEntity> findAllByFilters(@Param("uf") String uf, @Param("language") String language);
}

