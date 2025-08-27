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

    @Column("repository_url")
    private String repositoryUrl;

    @Column("branch_name ")
    private String branchName;
    private String status;

    @Column("start_time")
    private LocalDateTime startTime;

    @Column("end_time")
    private LocalDateTime endTime;

    @Column("error_message")
    private String errorMessage;

    @Column("storage_location")
    private String storageLocation;

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

