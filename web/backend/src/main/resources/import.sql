#disorder_comorbid_with_disorder
##out / disorder
create view disorder_comorbid_with_disorder_out as (with outdeg as (select id_1, count(id_1) as degree from disorder_comorbid_with_disorder group by id_1),outdist as(select degree , count(*) as count from outdeg group by degree)select * from outdist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from disorders) as nodes,(select sum(count) as count from outdist) as deg order by degree);
##in / disorder
create view disorder_comorbid_with_disorder_in as (with indeg as (select id_2, count(id_2) as degree from disorder_comorbid_with_disorder group by id_2),indist as(select degree as degree, count(*) as count from indeg group by degree)select * from indist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from disorders) as nodes,(select sum(count) as count from indist) as deg order by degree);

#disorder_is_subtype_of_disorder
##out / disorder
create view disorder_is_subtype_of_disorder_out as (with outdeg as (select id_1, count(id_1) as degree from disorder_is_subtype_of_disorder group by id_1),outdist as(select degree , count(*) as count from outdeg group by degree)select * from outdist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from disorders) as nodes,(select sum(count) as count from outdist) as deg order by degree);
##in / disorder
create view disorder_is_subtype_of_disorder_in as (with indeg as (select id_2, count(id_2) as degree from disorder_is_subtype_of_disorder group by id_2),indist as(select degree as degree, count(*) as count from indeg group by degree)select * from indist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from disorders) as nodes,(select sum(count) as count from indist) as deg order by degree);

#drug_has_indication
##out / drug
create view drug_has_indication_out as (with outdeg as (select id_1, count(id_1) as degree from drug_has_indication group by id_1),outdist as(select degree , count(*) as count from outdeg group by degree)select * from outdist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from drugs) as nodes,(select sum(count) as count from outdist) as deg order by degree);
##in / disorder
create view drug_has_indication_in as (with indeg as (select id_2, count(id_2) as degree from drug_has_indication group by id_2),indist as(select degree as degree, count(*) as count from indeg group by degree)select * from indist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from disorders) as nodes,(select sum(count) as count from indist) as deg order by degree);

#drug_has_target_protein
##out / drug
create view drug_has_target_protein_out as (with outdeg as (select id_1, count(id_1) as degree from drug_has_target_protein group by id_1),outdist as(select degree , count(*) as count from outdeg group by degree)select * from outdist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from drugs) as nodes,(select sum(count) as count from outdist) as deg order by degree);
##in / protein
create view drug_has_target_protein_in as (with indeg as (select id_2, count(id_2) as degree from drug_has_target_protein group by id_2),indist as(select degree as degree, count(*) as count from indeg group by degree)select * from indist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from proteins) as nodes,(select sum(count) as count from indist) as deg order by degree);

#drug_has_target_gene
##out / drug
create view drug_has_target_gene_out as (with outdeg as (select id_1, count(id_1) as degree from drug_has_target_gene group by id_1),outdist as(select degree , count(*) as count from outdeg group by degree)select * from outdist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from drugs) as nodes,(select sum(count) as count from outdist) as deg order by degree);
##in / gene
create view drug_has_target_gene_in as (with indeg as (select id_2, count(id_2) as degree from drug_has_target_gene group by id_2),indist as(select degree as degree, count(*) as count from indeg group by degree)select * from indist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from proteins) as nodes,(select sum(count) as count from indist) as deg order by degree);

#gene_associated_with_disorder
##out / gene
create view gene_associated_with_disorder_out as (with outdeg as (select id_1, count(id_1) as degree from gene_associated_with_disorder group by id_1),outdist as(select degree , count(*) as count from outdeg group by degree)select * from outdist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from genes) as nodes,(select sum(count) as count from outdist) as deg order by degree);
##in / disorder
create view gene_associated_with_disorder_in as (with indeg as (select id_2, count(id_2) as degree from gene_associated_with_disorder group by id_2),indist as(select degree as degree, count(*) as count from indeg group by degree)select * from indist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from disorders) as nodes,(select sum(count) as count from indist) as deg order by degree);

#protein_associated_with_disorder
##out / protein
create view protein_associated_with_disorder_out as (with outdeg as (select id_1, count(id_1) as degree from protein_associated_with_disorder group by id_1),outdist as(select degree , count(*) as count from outdeg group by degree)select * from outdist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from genes) as nodes,(select sum(count) as count from outdist) as deg order by degree);
##in / disorder
create view protein_associated_with_disorder_in as (with indeg as (select id_2, count(id_2) as degree from protein_associated_with_disorder group by id_2),indist as(select degree as degree, count(*) as count from indeg group by degree)select * from indist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from disorders) as nodes,(select sum(count) as count from indist) as deg order by degree);


#protein_encoded_by
##out / protein
create view protein_encoded_by_out as (with outdeg as (select id_1, count(id_1) as degree from protein_encoded_by group by id_1),outdist as(select degree , count(*) as count from outdeg group by degree)select * from outdist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from proteins) as nodes,(select sum(count) as count from outdist) as deg order by degree);
##in / gene
create view protein_encoded_by_in as (with indeg as (select id_2, count(id_2) as degree from protein_encoded_by group by id_2),indist as(select degree as degree, count(*) as count from indeg group by degree)select * from indist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from genes) as nodes,(select sum(count) as count from indist) as deg order by degree);

#protein_in_pathway
##out / protein
create view protein_in_pathway_out as (with outdeg as (select id_1, count(id_1) as degree from protein_in_pathway group by id_1),outdist as(select degree , count(*) as count from outdeg group by degree)select * from outdist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from proteins) as nodes,(select sum(count) as count from outdist) as deg order by degree);
##in / pathway
create view protein_in_pathway_in as (with indeg as (select id_2, count(id_2) as degree from protein_in_pathway group by id_2),indist as(select degree as degree, count(*) as count from indeg group by degree)select * from indist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from pathways) as nodes,(select sum(count) as count from indist) as deg order by degree);

#protein_interacts_with_protein
##out / protein
create view protein_interacts_with_protein_out as (with outdeg as (select id_1, count(id_1) as degree from protein_interacts_with_protein group by id_1),outdist as(select degree , count(*) as count from outdeg group by degree)select * from outdist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from proteins) as nodes,(select sum(count) as count from outdist) as deg order by degree);
##in / pathway
create view protein_interacts_with_protein_in as (with indeg as (select id_2, count(id_2) as degree from protein_interacts_with_protein group by id_2),indist as(select degree as degree, count(*) as count from indeg group by degree)select * from indist union select 0 as degree, nodes.count-deg.count as count from (select count(id) as count from proteins) as nodes,(select sum(count) as count from indist) as deg order by degree);


