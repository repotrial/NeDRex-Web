package de.exbio.reposcapeweb.db.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.TreeMap;

@Service
public class StatisticsService {


    private final StatisticsRepository statisticsRepository;

    @Autowired
    public StatisticsService(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    public TreeMap<Integer, Integer> getDisorderComorbidWithDisorderOut() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_disorder_comorbid_with_disorder_out().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }

    public TreeMap<Integer, Integer> getDisorderComorbidWithDisorderIn() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_disorder_comorbid_with_disorder_in().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }


    public TreeMap<Integer, Integer> getDisorderIsADisorderOut() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_disorder_is_a_disorder_out().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }

    public TreeMap<Integer, Integer> getDisorderIsADisorderIn() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_disorder_is_a_disorder_in().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }


    public TreeMap<Integer, Integer> getDrugHasIndicationOut() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_drug_has_indication_out().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }

    public TreeMap<Integer, Integer> getDrugHasIndicationIn() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_drug_has_indication_in().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }


    public TreeMap<Integer, Integer> getDrugHasTargetProteinOut() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_drug_has_target_protein_out().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }

    public TreeMap<Integer, Integer> getDrugHasTargetProteinIn() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_drug_has_target_protein_in().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }

    public TreeMap<Integer, Integer> getDrugHasTargetGeneOut() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_drug_has_target_gene_out().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }

    public TreeMap<Integer, Integer> getDrugHasTargetGeneIn() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_drug_has_target_gene_in().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }

    public TreeMap<Integer, Integer> getGeneAssociatedWithDisorderOut() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_gene_associated_with_disorder_out().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }

    public TreeMap<Integer, Integer> getGeneAssociatedWithDisorderIn() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_gene_associated_with_disorder_in().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }


    public TreeMap<Integer, Integer> getProteinAssociatedWithDisorderOut() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_protein_associated_with_disorder_out().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }

    public TreeMap<Integer, Integer> getProteinAssociatedWithDisorderIn() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_protein_associated_with_disorder_in().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }


    public TreeMap<Integer, Integer> getProteinEncodedByOut() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_protein_encoded_by_out().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }

    public TreeMap<Integer, Integer> getProteinEncodedByIn() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_protein_encoded_by_in().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }

    public TreeMap<Integer, Integer> getProteinInPathwayOut() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_protein_in_pathway_out().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }

    public TreeMap<Integer, Integer> getProteinInPathwayIn() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_protein_in_pathway_in().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }

    public TreeMap<Integer, Integer> getProteinInteractsWithProteinOut() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_protein_interacts_with_protein_out().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }

    public TreeMap<Integer, Integer> getProteinInteractsWithProteinIn() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        statisticsRepository.get_protein_interacts_with_protein_in().forEach(d -> result.put(d.getDegree(), d.getCount()));
        return result;
    }



}
