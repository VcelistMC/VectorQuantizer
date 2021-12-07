import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

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

    public ImageVector vectorAverage1D(ArrayList<ImageVector> imageVectors){
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

    public ImageVector vectorAverage(ArrayList<ArrayList<ImageVector>> imageVectors){
        ArrayList<ImageVector> vectors = new ArrayList<>();
        for (int row = 0; row < imageVectors.size(); row++){
            for (int col = 0; col < imageVectors.size(); col++){
                vectors.add(imageVectors.get(row).get(col));
            }
        }
        return vectorAverage1D(vectors);
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
