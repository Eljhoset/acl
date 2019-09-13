package com.example.testAcl.deep;

import com.querydsl.sql.H2Templates;
import com.querydsl.sql.SQLTemplates;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "spring.datasource.platform", havingValue = "h2", matchIfMissing = true)
public class H2SQLTemplatesServiceImpl implements SQLTemplatesService {

    @Override
    public SQLTemplates getTemplates() {
        return new H2Templates();

    }
}
