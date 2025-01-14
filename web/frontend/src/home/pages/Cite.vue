<template>
  <v-card dark :color="bgColor" elevation="5">
    <v-card-title>
      Cite
    </v-card-title>
    <v-card-text>

      <v-card-title>Plain</v-card-title>
      <v-textarea solo disabled no-resize :value=citationPlain rows="3">
      </v-textarea>
      <v-card-title>BibTeX</v-card-title>
      <v-textarea solo disabled no-resize :value="citationBibtex" rows="10"></v-textarea>
      <v-card-title>DOI URL: <a style="margin-left: 16px" target="_blank" href="https://doi.ieeecomputersociety.org/10.1109/BIBM62325.2024.10822390">https://doi.ieeecomputersociety.org/10.1109/BIBM62325.2024.10822390</a></v-card-title>
    </v-card-text>
  </v-card>
</template>

<script>
export default {
  name: "Cite",
  props: {
    bgColor: String,
    color: String,
  },
    data: () => ({
      citationPlain: "A. Maier et al., NeDRex-Web: An Interactive Web Tool for Drug Repurposing by Exploring Heterogeneous Molecular Networks, 2024 IEEE International Conference on Bioinformatics and Biomedicine (BIBM), Lisbon, Portugal, 2024, pp. 429-434, doi: 10.1109/BIBM62325.2024.10822390.",
      citationBibtex: "@INPROCEEDINGS {10822390,\n" +
        "author = { Maier, Andreas and Rafiei, Mahdie and Anastasi, Elisa and Zolotareva, Olga and Skelton, James and Elkjaer, Maria L. and Casas, Ana I. and Nogales, Cristian and Schmidt, Harald H. H. W. and Kacprowski, Tim and Blumenthal, David B. and Wipat, Anil and Sadegh, Sepideh and Baumbach, Jan },\n" +
        "booktitle = { 2024 IEEE International Conference on Bioinformatics and Biomedicine (BIBM) },\n" +
        "title = {{ NeDRex-Web: An Interactive Web Tool for Drug Repurposing by Exploring Heterogeneous Molecular Networks }},\n" +
        "year = {2024},\n" +
        "volume = {},\n" +
        "ISSN = {},\n" +
        "pages = {429-434},\n" +
        "abstract = { Finding new indications for approved drugs is a promising alternative to the often very lengthy and expensive process of de novo drug development. Systems medicine has brought forth several different approaches to tackle this important task. We recently published NeDRex, a network medicine tool for the identification of disease modules and drug repurposing. NeDRex-Web (https://web.nedrex.net) brings existing and new features of the NeDRex platform to a user-friendly and research-oriented web application, enabling online exploration of large heterogeneous molecular networks. Focusing mainly on drug repurposing, NeDRex-Web implements customizable disease module identification and drug prioritization workflows to support users of diverse backgrounds in their research. Users are assisted during every step of their analysis, including the definition of relevant input sets, the selection from various algorithms for module identification or drug prioritization, and the prioritization of the results by their statistical significance. A guided connectivity search provides an easy way to identify links between node sets of interest and can be used to create user-specific induced networks. },\n" +
        "keywords = {Drugs;Focusing;Knowledge graphs;Bioinformatics;Diseases},\n" +
        "doi = {10.1109/BIBM62325.2024.10822390},\n" +
        "url = {https://doi.ieeecomputersociety.org/10.1109/BIBM62325.2024.10822390},\n" +
        "publisher = {IEEE Computer Society},\n" +
        "address = {Los Alamitos, CA, USA},\n" +
        "month =Dec}\n"
  })
}
</script>

<style scoped>
b {
  color: #e5e5e5;
}
</style>
