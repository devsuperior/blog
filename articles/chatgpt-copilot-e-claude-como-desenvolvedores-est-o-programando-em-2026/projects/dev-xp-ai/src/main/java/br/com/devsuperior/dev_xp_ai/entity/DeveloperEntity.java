package br.com.devsuperior.dev_xp_ai.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("tb_developer")
public class DeveloperEntity {

    @Id
    private Long id;

    @Column("full_name")
    private String fullName;

    @Column("email")
    private String email;

    @Column("nickname")
    private String nickname;

    @Column("uf")
    private String uf;

    @Column("years_of_experience")
    private Integer yearsOfExperience;

    @Column("primary_language")
    private String primaryLanguage;

    @Column("interested_in_ai")
    private Boolean interestedInAi;

    @Column("skills")
    private String skills;

    public DeveloperEntity() {
    }

    public DeveloperEntity(Long id, String fullName, String email, String nickname,
                           String uf, Integer yearsOfExperience, String primaryLanguage,
                           Boolean interestedInAi, String skills) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.nickname = nickname;
        this.uf = uf;
        this.yearsOfExperience = yearsOfExperience;
        this.primaryLanguage = primaryLanguage;
        this.interestedInAi = interestedInAi;
        this.skills = skills;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public String getPrimaryLanguage() { return primaryLanguage; }
    public void setPrimaryLanguage(String primaryLanguage) { this.primaryLanguage = primaryLanguage; }

    public Boolean getInterestedInAi() { return interestedInAi; }
    public void setInterestedInAi(Boolean interestedInAi) { this.interestedInAi = interestedInAi; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
}

