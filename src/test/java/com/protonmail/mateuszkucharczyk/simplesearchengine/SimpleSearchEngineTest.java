package com.protonmail.mateuszkucharczyk.simplesearchengine;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

public class SimpleSearchEngineTest {
    @Test
    public void onSingleTermQuery_whenNoDocumentContainsThisTerm_thenReturnEmptyResult() {
        // arrange
        SimpleSearchEngine searchEngine = makeSimpleSearchEngine(
                new Document("Any document not containing given search term"),
                new Document("Any other document not containing given search term"));

        // act
        List<Document> searchResult = searchEngine.search("anyTermNotAppearingInAnyDocument");

        // assert
        assertThat(searchResult, empty());
    }

    @Test
    public void onSingleTermQuery_whenSingleDocumentContainsThisTerm_thenMatchThisDocument() {
        // arrange
        Document anyDocumentContainingTerm = new Document("the lazy brown dog sat in the corner");
        Document anyOtherDocument = new Document("the red fox bit the lazy dog");
        SimpleSearchEngine searchEngine = makeSimpleSearchEngine(
                anyDocumentContainingTerm,
                anyOtherDocument);
        // act
        List<Document> searchResult = searchEngine.search("brown");

        // assert
        assertThat(searchResult, contains(anyDocumentContainingTerm));
    }

    @Test
    public void onSingleTermQuery_whenMultipleDocumentsContainThisTerm_thenMatchingDocumentsAreSortedDescByTermFrequency() {
        // arrange
        Document anyDocumentContainingTermOnce = new Document("the lazy brown dog sat in the corner");
        Document anyDocumentContainingTermTwice = new Document("the brown fox jumped over the brown dog");
        Document anyOtherDocument = new Document("this document does not match");
        SimpleSearchEngine searchEngine = makeSimpleSearchEngine(
                anyDocumentContainingTermOnce,
                anyDocumentContainingTermTwice,
                anyOtherDocument);
        // act
        List<Document> searchResult = searchEngine.search("brown");

        // assert
        assertThat(searchResult, contains(anyDocumentContainingTermTwice, anyDocumentContainingTermOnce));
    }

    @Test
    public void onMultiTermQuery_whenTermsHaveSimilarRelevance_thenMatchingDocumentsAreSortedDescBySumOfTermsFrequency() {
        // arrange
        Document anyDocumentContainingTermsTwice = new Document("the lazy brown dog sat in the corner");
        Document anyDocumentContainingTermThrice = new Document("the brown fox jumped over the brown dog");
        Document anyDocumentContainingTermsOnce = new Document("the red fox bit the lazy dog");
        Document anyOtherDocument = new Document("this document does not match");
        SimpleSearchEngine searchEngine = makeSimpleSearchEngine(
                anyDocumentContainingTermsTwice,
                anyDocumentContainingTermThrice,
                anyDocumentContainingTermsOnce,
                anyOtherDocument);
        // act
        List<Document> searchResult = searchEngine.search("brown", "dog");

        // assert
        assertThat(searchResult, contains(
                anyDocumentContainingTermThrice,
                anyDocumentContainingTermsTwice,
                anyDocumentContainingTermsOnce));
    }

    @Test
    public void onMultiTermQuery_whenOneOfTermsIsVeryCommon_thenMatchingDocumentsAreSortedDescByTermsRelevance() {
        // arrange
        Document anyDocumentContainingRelevantTerms = new Document("a cow is relevant");
        Document anyDocumentContainingRelevantAndIrrelevantTerm = new Document("the cow is even more relevant");
        List<Document> documents = makeIrrelevantDocuments("the the the the the are irrelevant", 100);
        documents.add(anyDocumentContainingRelevantTerms);
        documents.add(anyDocumentContainingRelevantAndIrrelevantTerm);

        SimpleSearchEngine searchEngine = makeSimpleSearchEngine(documents);

        // act
        List<Document> searchResult = searchEngine.search("the", "cow");

        // assert
        assertThat(searchResult.get(0), is(anyDocumentContainingRelevantAndIrrelevantTerm));
        assertThat(searchResult.get(1), is(anyDocumentContainingRelevantTerms));
    }

    private static List<Document> makeIrrelevantDocuments(String content, int numberOfDocuments) {
        List<Document> documents = new LinkedList<>();
        for (int i = 0; i < numberOfDocuments; i++) {
            documents.add(new Document(content));
        }
        return documents;
    }

    private static SimpleSearchEngine makeSimpleSearchEngine(Document... documents) {
        return makeSimpleSearchEngine(asList(documents));
    }

    private static SimpleSearchEngine makeSimpleSearchEngine(List<Document> documents) {
        return new SimpleSearchEngine(documents);
    }
}