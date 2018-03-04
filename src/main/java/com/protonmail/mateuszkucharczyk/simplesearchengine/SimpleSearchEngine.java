package com.protonmail.mateuszkucharczyk.simplesearchengine;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.Comparator.comparingDouble;

public class SimpleSearchEngine {
    private final Multimap<String, Document> inverseIndex = HashMultimap.create();
    private final Multimap<Document, String> forwardIndex = LinkedListMultimap.create();

    private final Map<String, Map<Document, Double>> termFrequencyCache = new HashMap<>();
    private final Map<String, Double> inverseDocumentFrequencyCache = new HashMap<>();

    public SimpleSearchEngine(Collection<Document> documents) {
        addDocuments(documents);
    }

    public void addDocument(Document document) {
        addDocuments(singletonList(document));
    }

    public void addDocuments(Collection<Document> documents) {
        // first: index all documents
        // second: cache all terms of all new documents
        // this way you do not calculate cache of the same term several times
        documents.forEach(this::index);
        cacheTermFrequencyAndInverseTermFrequency(documents);
    }

    private void index(Document document) {
        String[] tokens = document.getContent().split("\\s");
        forwardIndex.putAll(document, asList(tokens));

        for (String token : tokens) {
            inverseIndex.put(token, document);
        }
    }

    private void cacheTermFrequencyAndInverseTermFrequency(Collection<Document> documents) {
        for (Document document : documents) {
            HashSet<String> termsToRecalculateTermFrequencyCache = uniqueTermsOf(document);
            for (String term : termsToRecalculateTermFrequencyCache) {
                cacheTermFrequency(term, document);
            }
        }

        Set<String> termsToRecalculateInverseDocumentFrequencyCache = uniqueTermsOf(documents);
        for (String term : termsToRecalculateInverseDocumentFrequencyCache) {
            cacheInverseDocumentFrequency(term);
        }
    }

    private Set<String> uniqueTermsOf(Collection<Document> documents) {
        return documents.stream()
                .map(this::uniqueTermsOf)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private HashSet<String> uniqueTermsOf(Document document) {
        return new HashSet<>(forwardIndex.get(document));
    }

    private void cacheTermFrequency(String term, Document document) {
        termFrequencyCache.putIfAbsent(term, new HashMap<>());
        termFrequencyCache.get(term).put(document, termFrequencyOf(term, document));
    }

    private double termFrequencyOf(String term, Document document) {
        return frequency(forwardIndex.get(document), term);
    }

    private void cacheInverseDocumentFrequency(String term) {
        inverseDocumentFrequencyCache.put(term, inverseDocumentFrequencyOf(term));
    }

    private double inverseDocumentFrequencyOf(String term) {
        double totalNumberOfDocuments = forwardIndex.keySet().size();
        double numberOfDocumentsContainingTerm = inverseIndex.get(term).size();
        return Math.log(totalNumberOfDocuments/numberOfDocumentsContainingTerm);
    }

    public List<Document> search(String... terms) {
        Set<Document> documentsContainingTerm = new HashSet<>();
        for (String term : terms) {
            documentsContainingTerm.addAll(inverseIndex.get(term));
        }

        ArrayList<Document> documentsSortedByRelevanceDesc = new ArrayList<>(documentsContainingTerm);
        documentsSortedByRelevanceDesc.sort(reverseOrder(byRelevance(terms)));
        return documentsSortedByRelevanceDesc;
    }

    private Comparator<Document> byRelevance(String... terms) {
        return comparingDouble(document -> relevanceOf(document, terms));
    }

    private double relevanceOf(Document document, String... terms) {
        double termsFrequency = 0;
        for (String term : terms) {
            termsFrequency += termFrequencyInverseDocumentFrequencyOf(document, term);
        }
        return termsFrequency;
    }

    private double termFrequencyInverseDocumentFrequencyOf(Document document, String term) {
        if (!termFrequencyCache.containsKey(term)) {
            return 0.0;
        }

        Double tf = termFrequencyCache.get(term).getOrDefault(document, 0.0);
        Double idf = inverseDocumentFrequencyCache.get(term);
        return tf * idf;
    }
}
