import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class VectorQuantizer {
    private int _vectorSize;
    private int _codeBookSize;
    private ArrayList<ArrayList<ImageVector>> _imageAsVectors;

    VectorQuantizer(int vectorSize, int codeBookSize){
        _vectorSize = vectorSize;
        _codeBookSize = codeBookSize;
        _imageAsVectors = new ArrayList<>();
    }

    private void _convertImageDataToVectors(ArrayList<ArrayList<Integer>> imageData){
        int width = imageData.size();
        int length = imageData.get(0).size();

        for(int row = 0; row < width; row += _vectorSize){
            ArrayList<ImageVector> currVectorsRow = new ArrayList<>();

            for(int col = 0; col < length; col += _vectorSize){
                ImageVector newVector = new ImageVector(_vectorSize);
                int currRowIndex = row;
                int currColIndex = col;

                while(currRowIndex < row + _vectorSize){
                    ArrayList<Integer> currRow;

                    if(currRowIndex >= width){
                        currRow = new ArrayList<>();
                        for(int i = 0; i < length; i++)
                            currRow.add(0);
                    }
                    else
                        currRow = imageData.get(currRowIndex);

                    while(currColIndex < col + _vectorSize){
                        int pixelValue = 0;

                        if(currColIndex < length)
                            pixelValue = currRow.get(currColIndex);
                        newVector.add(pixelValue);
                        currColIndex++;
                    }
                    currRowIndex++;
                    currColIndex = col;
                }
                currVectorsRow.add(newVector);
            }
            _imageAsVectors.add(currVectorsRow);
        }
    }

    public void compress(ArrayList<ArrayList<Integer>> imageData){
        _convertImageDataToVectors(imageData);
    }

    private ImageVector vectorAverage1D(ArrayList<ImageVector> imageVectors){
        ImageVector averageVector = new ImageVector(_vectorSize);
        for (int row = 0; row < _vectorSize; row++){
            for (int col = 0; col < _vectorSize; col++){
                int sum = 0;
                for (ImageVector imageVector : imageVectors){
                    sum += imageVector.getPixel(row, col);
                }
                averageVector.add(sum / imageVectors.size());
            }
        }
        return averageVector;
    }

    private ImageVector vectorAverage(ArrayList<ArrayList<ImageVector>> imageVectors){
        ArrayList<ImageVector> vectors = new ArrayList<>();
        for (int row = 0; row < imageVectors.size(); row++){
            for (int col = 0; col < imageVectors.size(); col++){
                vectors.add(imageVectors.get(row).get(col));
            }
        }
        return vectorAverage1D(vectors);
    }

    private ImageVector findMinimumDistance(ImageVector imageVector, ArrayList<ImageVector> keys){
        double minimumDistance = imageVector.getEuclideanDistanceTo(keys.get(0));
        int minimumDistanceIndex = 0;
        for (int i = 1; i < keys.size(); i++){
            double tempDistance = imageVector.getEuclideanDistanceTo(keys.get(i));
            if (minimumDistance > tempDistance){
                minimumDistance = tempDistance;
                minimumDistanceIndex = i;
            }
        }
        return keys.get(minimumDistanceIndex);
    }

    private ArrayList<ImageVector> generateCodeBook(){
        ImageVector averageVector = vectorAverage(_imageAsVectors);
        ArrayList<ImageVector> splittedVectors = averageVector.split();
        HashMap<ImageVector, ArrayList<ImageVector>> codeBook = new HashMap<>();
        while (codeBook.size() < _codeBookSize){
            for (ArrayList<ImageVector> row : _imageAsVectors){
                for (ImageVector imageVector : row){
                    ImageVector minimumKey = findMinimumDistance(imageVector, new ArrayList<>(codeBook.keySet()));
                    codeBook.get(minimumKey).add(imageVector);
                }
            }
        }
    }



    public static void main(String[] args) {
        ArrayList<ArrayList<Integer>> data = new ArrayList<ArrayList<Integer>>(
            Arrays.asList(
                new ArrayList<Integer>(
                    Arrays.asList(1, 2, 7, 9)
                ),
                new ArrayList<Integer>(
                    Arrays.asList(3, 4, 6, 6)
                ),
                new ArrayList<Integer>(
                    Arrays.asList(4, 9, 15, 14)
                ),
                new ArrayList<Integer>(
                    Arrays.asList(10, 10, 20, 18)
                )
            )
        );

        VectorQuantizer vectorQuantizer = new VectorQuantizer(3, 4);
        vectorQuantizer.compress(data);
    }
}
