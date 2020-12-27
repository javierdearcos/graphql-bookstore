package es.javierdearcos.graphql.bookstore;

import com.google.common.io.Resources;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author Javier de Arcos
 */
@Component
public class GraphQLProvider {

    private GraphQL graphQL;

    private GraphQLDataFetchers graphQLDataFetchers;


    public GraphQLProvider(GraphQLDataFetchers graphQLDataFetchers) {
        this.graphQLDataFetchers = graphQLDataFetchers;
    }

    @PostConstruct
    public void init() throws IOException {
        URL url = Resources.getResource("schema.graphqls");
        String sdl = Resources.toString(url, StandardCharsets.UTF_8);
        GraphQLSchema graphQLSchema = buildGraphQLSchema(sdl);
        graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    private GraphQLSchema buildGraphQLSchema(String sdl) {
        TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("bookById", graphQLDataFetchers.getBookByIdDataFetcher()))
                .type(TypeRuntimeWiring.newTypeWiring("Book")
                        .dataFetcher("author", graphQLDataFetchers.getAuthorDataFetcher())
                        .dataFetcher("pageCount", graphQLDataFetchers.getPageCountDataFetcher()))
                .build();
    }

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }
}
