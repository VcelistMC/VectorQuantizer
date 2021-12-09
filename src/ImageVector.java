import java.util.ArrayList;

class ImageVector {
    private ArrayList<ArrayList<Integer>> _vectorData;
    private int _size;
    private String _binaryCode; 
    
    // for the purposes of adding pixels incrementally
    private int _currRow = 0;
    private int _currCol = 0;

    public ImageVector(int size){
        _size = size;
        _vectorData = new ArrayList<>();
        for(int i = 0; i < size; i++){
            ArrayList<Integer> row = new ArrayList<>();
            for(int j = 0; j < size; j++)
                row.add(0);
            _vectorData.add(row);
        }
    }

    public ImageVector(ArrayList<ArrayList<Integer>> vectorData){
        _size = vectorData.size();
        _vectorData = vectorData;
    }

    public void setBinaryCode(String code){
        _binaryCode = code;
    }

    @Override
    public String toString() {
        String out = "";
        for(ArrayList<Integer> row: _vectorData){
            for(int pixel : row){
                out += (pixel + " ");
            }
            out += "|";
        }
        return out;
    }

    public int getPixelInc(){
        _currCol = 0;
        _currRow = 0;
        if(_currCol == _size){
            _currCol = 0;
            _currRow++;
        }
        if(_currRow == _size){
            return 0;
        }
        return getPixel(_currRow, _currCol);
    }


    public void printVector(){
        for(ArrayList<Integer> row : _vectorData){
            for(Integer pixel: row){
                System.out.print(pixel + " ");
            }
            System.out.println();
        }
    }

    /**
     * adds a pixel to a vector incrementally,
     * automatically going down one row when we reach column bound
     */
    public void add(int pixel){
        if(_currCol == _size){
            _currCol = 0;
            _currRow++;
        }
        if(_currRow == _size){
            return;
        }
        setPixel(_currRow, _currCol, pixel);
        _currCol++;

    }

    public ArrayList<ImageVector> split(){
        ArrayList<ImageVector> splittedVectors = new ArrayList<>();
        ImageVector vec1 = new ImageVector(_size);
        ImageVector vec2 = new ImageVector(_size);
        int currCol = 0, currRow = 0;
        
        while (true) {
            if(currCol == this._size){
                currCol = 0;
                currRow++;
            }
            if(currRow == this._size){
                break;
            }
            
            int currPixel = getPixel(currRow, currCol);

            vec1.add(currPixel - 1);
            vec2.add(currPixel + 1);
            currCol++;
            
        }
        splittedVectors.add(vec1);
        splittedVectors.add(vec2);
        return splittedVectors;
    }


    public double getEuclideanDistanceTo(ImageVector that){
        double distance = 0.0;
        int currCol = 0, currRow = 0;
        double sum = 0;

        while(true){
            if(currCol == _size){
                currCol = 0;
                currRow++;
            }
            if(currRow == _size){
                break;
            }
            int firstPixel = this.getPixel(currRow, currCol);
            int secondPixel = that.getPixel(currRow, currCol);
            
            sum += Math.pow(firstPixel - secondPixel, 2);
            currCol++;
        }

        distance = Math.sqrt(sum);
        return distance;
    }

    public void setPixel(int row, int col, int val){
        _vectorData.get(row).set(col, val);
    }

    public int getPixel(int row, int col){
        return _vectorData.get(row).get(col); 
    }

    public void setRow(int row, ArrayList<Integer> rowData){
        _vectorData.set(row, rowData);
    }

    public boolean equals(ImageVector that){
        for (int row = 0; row < _size; row++){
            for (int col = 0; col < _size; col++){
                if (this.getPixel(row, col) != that.getPixel(row, col)){
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        ImageVector vec = new ImageVector(2);
        vec.setPixel(1, 1, 231);
    }

    public String getBinaryCode() {
        return _binaryCode;
    }
}