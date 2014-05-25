package prev_work;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

public class PatternNonSeleniumFinder {

    public static void ProcessUrlsAndHTMLSize(ArrayList<PageInfo> pageInfo) {

        // calculo para padrao SORT
        PatternWeightCalculator sortCheck = new PatternWeightCalculator("Sort",
                0.0);

        // calcular media do tamanho dos HTMLs
        double fileSizeAverage = FileSizeMath.calculateAverageFileSize(System
                .getProperty("user.dir") + "\\HTMLfinal\\");
        System.out.println("\tfileSizeAverage: "+fileSizeAverage);

        // keywords que podem indicar a presença de sort
        String[] sortKeywords = { "desc", "asc", "DESC", "ASC", "orderby",
                "sort", "sortType", "SORT", "Sort", "sorttype" };
        Vector<String> sortKeywordsVector = new Vector<String>(
                Arrays.asList(sortKeywords));

        // para cada url
        for (int i = 0; i != pageInfo.size(); i++) {

            // Array List para guardar as keywords que estao no url
            ArrayList<String> keywordsInUrl = new ArrayList<String>();

            // vai ver que keywords há nesse url
            for (int j = 0; j != sortKeywordsVector.size(); j++) {
                String keyword = sortKeywordsVector.get(j).toString();
                if (pageInfo.get(i).getPageURL().contains(keyword)) {
                    keywordsInUrl.add(keyword);
                    System.out.println("\tkeyword: "+keyword);
                }
            }

            // calcular diferencial de tamanho de ficheiro
            double ratio = 0;
            if (i != 0)
                ratio = FileSizeMath
                        .calculateFileSizeByFileIndexAndCompareWithAverage(
                                i + 1, fileSizeAverage);

            // gravar para o ficheiro o url, os conteudos de keywords in Url e o
            // diferencial do tamanho
            // do ficheiro
            Filesystem.saveToFile("final", Integer.toString(i + 1)
                    + "extraInfo",
                    "URL:" + pageInfo.get(i).getPageURL() + "\n", true);
            System.out.println("\tratio:"+ratio);
            for (String keyword : keywordsInUrl) {
                // Se as keywords existirem, peso é adicionado ao SortCheck
                if (keyword.equalsIgnoreCase("sort")
                        || keyword.equalsIgnoreCase("sortType"))
                    sortCheck.addToWeight(0.9);
                else
                    sortCheck.addToWeight(0.6);

                System.out.println(keyword);
                Filesystem.saveToFile("final", Integer.toString(i + 1)
                        + "extraInfo",
                        "PRESENT-SORT-KEYWORD:" + keyword + "\n", true);
            }
            Filesystem.saveToFile("final", Integer.toString(i + 1)
                    + "extraInfo", "RATIOTOTAL:" + Double.toString(ratio)
                    + "\n", true);

            if (ratio < 0.15)
                sortCheck.addToWeight(0.4);

            double compareWithPrevious = 0;
            if (i != 0)
                compareWithPrevious = FileSizeMath
                        .compareHTMLSizeWithPreviousFile(i, i + 1);

            if (compareWithPrevious < 5)
                sortCheck.addToWeight(0.4);

            // se a soma das métricas for igual ou superior a 1, adiciona o
            // pattern
            sortCheck.checkPattern();

            Filesystem.saveToFile("final", Integer.toString(i + 1)
                    + "extraInfo",
                    "RATIOPREVIOUS:" + Double.toString(compareWithPrevious)
                            + "\n", true);
            Filesystem.saveToFile(
                    "final",
                    Integer.toString(i + 1) + "extraInfo",
                    "SELENIUMSTEP:"
                            + Integer.toString(pageInfo.get(i)
                                    .getSeleniumStepCorrespondent()) + "\n",
                    true);
        }

    }

    public static void testForMasterDetail() {
        int i = 2;
        String[] listOfPatternKeywords = { "LOGIN", "SEARCH" };
        // Boolean otherPatternsExist=false;
        while (true) {
            File file = new File(System.getProperty("user.dir") + "\\HTML"
                    + "final" + "\\" + i + ".txt");
            if (!file.exists())
                break;
            for (int j = 0; j != listOfPatternKeywords.length; j++) {
                // if other patterns exists that prevent Master detail from
                // existing, such as Login and Search
                // the check for master detail pattern will not be made in that
                // page
                if (Filesystem.searchWordInFile(listOfPatternKeywords[j],
                        "final", Integer.toString(i) + "extrainfo"))
                    break;

                if (Math.abs(Filesystem.retrieveNumericParameter(
                        "RATIOPREVIOUS", "final", Integer.toString(i)
                                + "extrainfo")) > 5.0) {
                    break;
                }

                if (Filesystem
                        .numberOfLinesInFile("final", Integer.toString(i)) > 5) {
                    break;
                }

                Filesystem.saveToFile("final", Integer.toString(i)
                        + "extraInfo", "POSSIBLE-MASTER-DETAIL" + "\n", true);

                // registo para padrao MasterDetail
                PatternWeightCalculator masterDetailCheck = new PatternWeightCalculator(
                        "MasterDetail", 1.0);
                masterDetailCheck.checkPattern();
            }

            i++;

        }

        // System.out.println(Filesystem.numberOfLinesInFile("final","1extrainfo"));

        // Boolean b=Filesystem.searchWordInFile("LOGIN","final","3extrainfo");

        // Filesystem.retrieveNumericParameter("RATIOPREVIOUS", "final",
        // "3extrainfo");

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        testForMasterDetail();

    }

}
