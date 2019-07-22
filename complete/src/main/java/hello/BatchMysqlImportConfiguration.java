package hello;

import javax.sql.DataSource;
import javax.xml.bind.Marshaller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableBatchProcessing
public class BatchMysqlImportConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    DataSource dataSource;

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    
	@Bean
	public JdbcCursorItemReader<Person> customerItemReader(DataSource dataSource) {
		return new JdbcCursorItemReaderBuilder<Person>()
				.name("customerItemReader")
				.dataSource(dataSource)
				.sql("select * from people")
				.rowMapper(new PrsonRowMapper())
				//.preparedStatementSetter(citySetter(null))
				.build();
	}

    //	@Bean
//	@StepScope
//	public ArgumentPreparedStatementSetter citySetter(
//			@Value("#{jobParameters['city']}") String city) {
//
//		return new ArgumentPreparedStatementSetter(new Object [] {city});
//	}

    @Bean
    public StaxEventItemWriter<Person> writer(DataSource dataSource) {

        StaxEventItemWriter<Person> staxItemWriter = new StaxEventItemWriter();
        FileSystemResource resource = new FileSystemResource("outputFile.xml");

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Person.class);
        staxItemWriter.setResource(resource);
        staxItemWriter.setMarshaller(marshaller);
        staxItemWriter.setRootTagName("people");
        staxItemWriter.setOverwriteOutput(true);

        return staxItemWriter;
    }
    // end::readerwriterprocessor[]

    // tag::jobstep[]
    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilderFactory.get("importUserFromJdbcJob")
            .incrementer(new RunIdIncrementer())
            .listener(listener)
            .flow(step1)
            .end()
            .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
            .<Person, Person> chunk(10)
            .reader(customerItemReader(dataSource))
            .processor(processor())
            .writer(writer(dataSource))
            .build();
    }
    // end::jobstep[]
}
