/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with
 the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/MPL-1.1.html
 
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.
 
 The Original Code is the "SensorML AbstractProcessing Engine".
 
 The Initial Developer of the Original Code is the VAST team at the
 
 Contributor(s): 
    Alexandre Robin <robin@nsstc.uah.edu>
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorML.process;

import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.Vector;
import org.vast.process.*;
import org.vast.math.*;
import org.vast.physics.*;
import org.vast.sensorML.ExecutableProcessImpl;


/**
 * <p>
 * Converts Lat/Lon/Alt coordinates to ECEF
 * </p>
 *
 * @author Alexandre Robin
 * @date Sep 2, 2005
 */
public class LLAToECEF_Process extends ExecutableProcessImpl
{
    private DataComponent latData, lonData, altData;
    private DataComponent lrxData, lryData, lrzData;
    private DataComponent outputPos;
    private Matrix4d toEcefMatrix = new Matrix4d();;
    private Matrix3d rotMatrix = new Matrix3d();;
    private boolean nadirOriented = true;
    private int upAxis = 3;
    private int northAxis = 2;
    char[] rotationOrder = {'Z','X','Y'};
    private Datum datum;


    public LLAToECEF_Process()
    {
    }

    
    public void init() throws SMLProcessException
    {
    	try
        {
            // get input data containers + create appropriate Unit Converters
    		DataComponent locationData = inputData.getComponent("geoLocation");
            latData = locationData.getComponent("latitude");          
            lonData = locationData.getComponent("longitude");       
            altData = locationData.getComponent("altitude");
            
            // get orientation data containers + create appropriate Unit Converters
            DataComponent orientationData = inputData.getComponent("localOrientation");
            lrxData = orientationData.getComponent("x");          
            lryData = orientationData.getComponent("y");    
            lrzData = orientationData.getComponent("z");
            // read rotation order
            DataComponent rotComp = inputData.getComponent("rotationOrder");
            if(rotComp != null) {
	            String rotOrder = rotComp.getData().getStringValue();
	            if (rotOrder != null && rotOrder.length() == 3)
	            	rotationOrder = rotOrder.toCharArray();
            }
            // set up upAxis and northAxis depending on the frame (ENU, NED, etc...)
            String refFrame = ((Vector)orientationData).getReferenceFrame();
            if (refFrame != null)
            {
            	if (refFrame.contains("NED"))
            	{
            		northAxis = 1;
            		upAxis = -3;
                    nadirOriented = true;
            	}
            	else if (refFrame.contains("ENU"))
            	{
            		northAxis = 2;
            		upAxis = 3;
                    nadirOriented = true;
            	}
            }
            
            // output data containers
        	outputPos = outputData.getComponent("ecefPosition");
        	outputPos.renewDataBlock();
        	
        	// set Datum to WGS84
        	datum = new Datum();
        }
        catch (Exception e)
        {
            throw new SMLProcessException(ioError, e);
        }
    }
   
    
    public void execute() throws SMLProcessException
    {
    	// get lat,lon,alt coordinates from input and convert to SI
    	double lat = latData.getData().getDoubleValue(0);
    	double lon = lonData.getData().getDoubleValue(0);
    	double alt = altData.getData().getDoubleValue(0);
        
        // convert to ECEF
        double[] ecefPos = MapProjection.LLAtoECF(lon, lat, alt, datum);
        toEcefMatrix.setIdentity();
                
        // compute nadir orientation if needed
        // default = north/east/up orientation
        if (nadirOriented == true)
        {
        	Vector3d ecfPosition;
        	ecfPosition = new Vector3d(ecefPos[0], ecefPos[1], ecefPos[2]);
            Vector3d toEcfNorth = NadirPointing.getEcfVectorToNorth(ecfPosition);
            Matrix3d nadirMatrix = NadirPointing.getRotationMatrix(ecfPosition, toEcfNorth, northAxis, upAxis);
            toEcefMatrix.setRotation(nadirMatrix);
        }
        
        // add translation coordinates
        for (int i=0; i<3; i++)
        	toEcefMatrix.setElement(i, 3, ecefPos[i]);
        
        // apply rotations in specified order
        computeRotMatrix();
        toEcefMatrix.mul(rotMatrix);
        
        // set output matrix values
        for (int i=0; i<15; i++)
        	outputPos.getData().setDoubleValue(i, toEcefMatrix.getElement(i/4, i%4));
    }
    
    
    /**
     * Computes attitude matrix with respect to local nadir orientation
     * @return 3x3 orientation matrix
     */
    protected void computeRotMatrix()
	{
        rotMatrix.setIdentity();
        
        // retrieve rotation values (converted to SI)
    	double rx = lrxData.getData().getDoubleValue();
    	double ry = lryData.getData().getDoubleValue();
    	double rz = lrzData.getData().getDoubleValue();

		// rotate in reverse order as the one given
        // because we want the opposite matrix
		for (int i=0; i<3; i++)
		{
			char axis = rotationOrder[2-i];
			
			switch (axis)
			{
				case 'X':
                    rotMatrix.rotateX(-rx);
					break;
					
				case 'Y':
                    rotMatrix.rotateY(-ry);
					break;
					
				case 'Z':
                    rotMatrix.rotateZ(rz); // reverse rz 
					break;
			}
		}
	}
}