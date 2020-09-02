package de.exbio.reposcapeweb.db.statistics;

import de.exbio.reposcapeweb.db.entities.nodes.Disorder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatisticsRepository extends CrudRepository<Disorder,Integer> {

    @Query(nativeQuery = true, value = "SELECT * FROM disorder_comorbid_with_disorder_out")
    List<Degrees> get_disorder_comorbid_with_disorder_out();

    @Query(nativeQuery = true, value = "SELECT * FROM disorder_comorbid_with_disorder_in")
    List<Degrees> get_disorder_comorbid_with_disorder_in();


    @Query(nativeQuery = true, value = "SELECT * FROM disorder_is_a_disorder_out")
    List<Degrees> get_disorder_is_a_disorder_out();

    @Query(nativeQuery = true, value = "SELECT * FROM disorder_is_a_disorder_in")
    List<Degrees> get_disorder_is_a_disorder_in();


    @Query(nativeQuery = true, value = "SELECT * FROM drug_has_indication_out")
    List<Degrees> get_drug_has_indication_out();

    @Query(nativeQuery = true, value = "SELECT * FROM drug_has_indication_in")
    List<Degrees> get_drug_has_indication_in();


    @Query(nativeQuery = true, value = "SELECT * FROM drug_has_target_out")
    List<Degrees> get_drug_has_target_out();

    @Query(nativeQuery = true, value = "SELECT * FROM drug_has_target_in")
    List<Degrees> get_drug_has_target_in();


    @Query(nativeQuery = true, value = "SELECT * FROM gene_associated_with_disorder_out")
    List<Degrees> get_gene_associated_with_disorder_out();

    @Query(nativeQuery = true, value = "SELECT * FROM gene_associated_with_disorder_in")
    List<Degrees> get_gene_associated_with_disorder_in();


    @Query(nativeQuery = true, value = "SELECT * FROM protein_encoded_by_out")
    List<Degrees> get_protein_encoded_by_out();

    @Query(nativeQuery = true, value = "SELECT * FROM protein_encoded_by_in")
    List<Degrees> get_protein_encoded_by_in();


    @Query(nativeQuery = true, value = "SELECT * FROM protein_in_pathway_out")
    List<Degrees> get_protein_in_pathway_out();

    @Query(nativeQuery = true, value = "SELECT * FROM protein_in_pathway_in")
    List<Degrees> get_protein_in_pathway_in();


    @Query(nativeQuery = true, value = "SELECT * FROM protein_interacts_with_protein_out")
    List<Degrees> get_protein_interacts_with_protein_out();

    @Query(nativeQuery = true, value = "SELECT * FROM protein_interacts_with_protein_in")
    List<Degrees> get_protein_interacts_with_protein_in();
}
