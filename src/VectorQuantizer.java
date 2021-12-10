import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class VectorQuantizer {
    private int _vectorSize;
    private int _codeBookSize;
    private ArrayList<ArrayList<ImageVector>> _imageAsVectors;
    private ArrayList<ArrayList<Integer>> _imageData;
    HashMap<ImageVector, ArrayList<ImageVector>> _codeBook;
    private int _width;
    private int _length;
    private HashMap<ImageVector, String> _binaryCodeBook;
    private ArrayList<ArrayList<String>> _imageAsBinCodes;
    private ArrayList<ArrayList<ImageVector>> _decompressedImageVectors;
    private ArrayList<ArrayList<Integer>> _decompressedImageData;

    VectorQuantizer(){
        _imageAsVectors = new ArrayList<>();
        _imageData = new ArrayList<>();
        _codeBook = new HashMap<>();
        _binaryCodeBook = new HashMap<>();
        _imageAsBinCodes = new ArrayList<>();
        _decompressedImageVectors = new ArrayList<>();
        _decompressedImageData = new ArrayList<>();
    }

    private void _clear(){
        _imageAsVectors.clear();
        _imageData.clear();
        _codeBook.clear();
        _binaryCodeBook.clear();
        _imageAsBinCodes.clear();
        _decompressedImageVectors.clear();
        _decompressedImageData.clear();
        _width = 0;
        _length = 0;
    }

    private void _ImageDataToVectors(){
        for(int row = 0; row < _width; row += _vectorSize){
            ArrayList<ImageVector> currVectorsRow = new ArrayList<>();

            for(int col = 0; col < _length; col += _vectorSize){
                ImageVector newVector = new ImageVector(_vectorSize);
                int currRowIndex = row;
                int currColIndex = col;

                while(currRowIndex < row + _vectorSize){
                    ArrayList<Integer> currRow;

                    if(currRowIndex >= _width){
                        currRow = new ArrayList<>();
                        for(int i = 0; i < _length; i++)
                            currRow.add(0);
                    }
                    else
                        currRow = _imageData.get(currRowIndex);

                    while(currColIndex < col + _vectorSize){
                        int pixelValue = 0;

                        if(currColIndex < _length)
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

    public void compress(String path, int vectorSize, int codeBookSize){
        _clear();
        _vectorSize = vectorSize;
        _codeBookSize = codeBookSize;
        System.out.println("Converting to 2d array");
        _imageToImageData(path);

        System.out.println("Converting to vectors");
        _ImageDataToVectors();
        
        System.out.println("Generating codebook");
        _generateCodeBook();
        
        System.out.println("assigning codees to codebook");
        _assignCodes();
        
        System.out.println("Encoding");
        _encode();
        
        System.out.println("Decoding");
        decompress();
    }

    private void _assignCodes(){
        int i = 0;
        for(ImageVector keyVector: _codeBook.keySet()){
            String bin = Integer.toBinaryString(i);
            ArrayList<ImageVector> children = _codeBook.get(keyVector);
            for(ImageVector child: children){
                child.setBinaryCode(bin);
            }
            _binaryCodeBook.put(keyVector, bin);
            i++;
        }
    }

    private void _encode(){
        for(ArrayList<ImageVector> row: _imageAsVectors){
            ArrayList<String> newRow = new ArrayList<>();
            for(ImageVector vector: row){
                newRow.add(vector.getBinaryCode());
            }
            _imageAsBinCodes.add(newRow);
        }
    }



    public void decompress() {
        for(ArrayList<String> row: _imageAsBinCodes){
            ArrayList<ImageVector> newRow = new ArrayList<>();
            for(String key: row){
                newRow.add(_getKeyFromValue(key, _binaryCodeBook));
            }
            _decompressedImageVectors.add(newRow);
        }

        _decompressedImageData = _vectorsToImageData();
        _writeImage(_decompressedImageData);
    }

    private ArrayList<ArrayList<Integer>> _vectorsToImageData() {
        ArrayList<ArrayList<Integer>> decompressedImageData = new ArrayList<>();
        for(ArrayList<ImageVector> row: _decompressedImageVectors){
            for(int rowInt = 0; rowInt < _vectorSize; rowInt++){
                ArrayList<Integer> newRow = new ArrayList<>();
                for(ImageVector vector: row){
                    for(int colInt = 0; colInt < _vectorSize; colInt++){
                        newRow.add(vector.getPixel(rowInt, colInt));
                    }
                }
                decompressedImageData.add(newRow);
            }
        }
        return decompressedImageData;
    }

    private void _writeImage(ArrayList<ArrayList<Integer>> data){
        BufferedImage image = new BufferedImage(_width, _length, BufferedImage.TYPE_INT_RGB);
        for(int i = 0; i < _width; i++){
            for(int j = 0; j < _length; j++){
                int pixel = data.get(i).get(j);
                pixel = pixel + (pixel << 8 ) + (pixel << 16);
                image.setRGB(i, j, pixel);
            }
        }
        File imgFile = new File("after.png");
        try {
            ImageIO.write(image, "png", imgFile);
        } catch (Exception e) {
            //TODO: handle exception
        }
    }

    private ImageVector _getKeyFromValue(String value, HashMap<ImageVector, String> hashMap){
        for(ImageVector key: hashMap.keySet()){
            String stringKey = hashMap.get(key);
            if(stringKey.equals(value))
                return key;
        }
        return null;
    }

    private void _imageToImageData(String imagePath){
        File file = new File(imagePath);
        BufferedImage img = null;
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        _width = img.getWidth();
        _length = img.getHeight();
        ArrayList<ArrayList<Integer>> imgArr = new ArrayList<>();
        Raster raster = img.getData();

        for (int i = 0; i < _width; i++) {
            imgArr.add(new ArrayList<>());
            for (int j = 0; j < _length; j++) {
                imgArr.get(i).add(raster.getSample(i, j, 0));
            }
        }
        _imageData = imgArr;
    }

    private ImageVector _vectorAverage1D(ArrayList<ImageVector> imageVectors){
        if(imageVectors.size() == 0){
            return new ImageVector(_vectorSize);
        }
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

    private ImageVector _vectorAverage(ArrayList<ArrayList<ImageVector>> imageVectors){
        ArrayList<ImageVector> vectors = new ArrayList<>();
        for (int row = 0; row < imageVectors.size(); row++){
            for (int col = 0; col < imageVectors.get(row).size(); col++){
                vectors.add(imageVectors.get(row).get(col));
            }
        }
        return _vectorAverage1D(vectors);
    }

    private ImageVector _findMinimumDistance(ImageVector imageVector, ArrayList<ImageVector> keys){
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

    private void _generateCodeBook(){
        ImageVector averageVector = _vectorAverage(_imageAsVectors);
        ArrayList<ImageVector> splittedVectors = averageVector.split();
        _codeBook.put(splittedVectors.get(0), new ArrayList<>());
        _codeBook.put(splittedVectors.get(1), new ArrayList<>());

        while (_codeBook.size() != _codeBookSize){
            HashMap<ImageVector, ArrayList<ImageVector>> tmpCodeBook = new HashMap<>();
            _assignVectors(_codeBook);

            for(ImageVector imageVector : _codeBook.keySet()){
                ImageVector avgVector = _vectorAverage1D(_codeBook.get(imageVector));
                splittedVectors = avgVector.split();
                tmpCodeBook.put(splittedVectors.get(0), new ArrayList<>());
                tmpCodeBook.put(splittedVectors.get(1), new ArrayList<>());
            }
            _codeBook = tmpCodeBook;
        }

        boolean vectorsChanged = true;
        int loopLimit = 100;
        while(vectorsChanged && loopLimit > 0){
            _assignVectors(_codeBook);

            HashMap<ImageVector, ArrayList<ImageVector>> tmpCodeBook = new HashMap<>();
            for(ImageVector imageVector : _codeBook.keySet()){
                ImageVector avgVector = _vectorAverage1D(_codeBook.get(imageVector));
                tmpCodeBook.put(avgVector, new ArrayList<>());
            }

            ArrayList<ImageVector> codeBookKeys = new ArrayList<>(_codeBook.keySet());
            ArrayList<ImageVector> tempCodeBookKeys = new ArrayList<>(tmpCodeBook.keySet());

            vectorsChanged = _didVectorsChange(codeBookKeys, tempCodeBookKeys);
            if(vectorsChanged)
                _codeBook = tmpCodeBook;
            loopLimit--;
            // System.out.println("loops:" + loopLimit);
        }
        if(vectorsChanged)
            _assignVectors(_codeBook);
    }

    private boolean _didVectorsChange(ArrayList<ImageVector> oldKeys, ArrayList<ImageVector> newKeys){
        for(int i = 0; i < oldKeys.size(); i++){
            ImageVector key1 = oldKeys.get(i);
            ImageVector key2 = newKeys.get(i);
            if (!key1.equals(key2))
                return true;
        }
        return false;
    }

    private void _assignVectors(HashMap<ImageVector, ArrayList<ImageVector>> codeBook){
        for (ArrayList<ImageVector> row : _imageAsVectors){
            for (ImageVector imageVector : row){
                ImageVector minimumKey = _findMinimumDistance(imageVector, new ArrayList<>(codeBook.keySet()));
                codeBook.get(minimumKey).add(imageVector);
            }
        }
    }

    public static void main(String[] args) {
        ArrayList<ArrayList<Integer>> data = new ArrayList<ArrayList<Integer>>(
                Arrays.asList(
                        new ArrayList<Integer>(
                                Arrays.asList(1, 2, 7, 9, 4, 11)
                        ),
                        new ArrayList<Integer>(
                                Arrays.asList(3, 4, 6, 6, 12, 12)
                        ),
                        new ArrayList<Integer>(
                                Arrays.asList(4, 9, 15, 14, 9, 9)
                        ),
                        new ArrayList<Integer>(
                                Arrays.asList(10, 10, 20, 18, 8, 8)
                        ),
                        new ArrayList<Integer>(
                                Arrays.asList(4, 3, 17, 16, 1, 4)
                        ),
                        new ArrayList<Integer>(
                                Arrays.asList(4, 5, 18, 18, 5, 6)
                        )
                )
        );

        VectorQuantizer vectorQuantizer = new VectorQuantizer();
        // vectorQuantizer._ImageDataToVectors(data);
        // for(ArrayList<ImageVector> row: vectorQuantizer._imageAsVectors){
        //     for(ImageVector vector: row){
        //         vector.printVector();
        //         System.out.println();
        //     }
        // }
        // vectorQuantizer.compress("C:\\Users\\Peter\\Desktop\\Desktop\\bg.png");
        vectorQuantizer.compress("output.png", 2, 32);
    }
}