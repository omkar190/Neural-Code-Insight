package com.neuralcode.insight.entity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("code_analysis")
@Data
@NoArgsConstructor
public class CodeAnalysis implements Persistable<String> {

    @Id
    private String id;
    private String repositoryUrl;
    private String branchName;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column("error_message")
    private String errorMessage;

    @Transient
    @Setter(AccessLevel.NONE)
    private boolean isNew = true;

    // Only custom methods
    @Override
    @Transient
    public boolean isNew() {
        return isNew;
    }

    public CodeAnalysis markNotNew() {
        this.isNew = false;
        return this;
    }

    public CodeAnalysis(String id, String repositoryUrl, String branchName, String status, LocalDateTime startTime) {
        this.id = id;
        this.repositoryUrl = repositoryUrl;
        this.branchName = branchName;
        this.status = status;
        this.startTime = startTime;
        this.isNew = true;
    }
}

