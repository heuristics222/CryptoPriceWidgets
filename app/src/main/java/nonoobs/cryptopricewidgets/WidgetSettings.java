package nonoobs.cryptopricewidgets;

/**
 * Created by Doug on 2017-05-05.
 */

public class WidgetSettings
{
    public static final int SOURCE_GDAX = 0;

    public WidgetSettings(int id)
    {
        setID(id);
    }

    public int getSource()
    {
        return mSource;
    }

    public void setSource(int source)
    {
        mSource = source;
    }

    public String getProduct()
    {
        return mProduct;
    }

    public void setProduct(String product)
    {
        mProduct = product;
    }

    public int getID()
    {
        return mID;
    }

    public void setID(int ID)
    {
        mID = ID;
    }

    private int mID;
    private int mSource;
    private String mProduct;
}
