/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with
 the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/MPL-1.1.html
 
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.
 
 The Original Code is the "SensorML DataProcessing Engine".
 
 The Initial Developer of the Original Code is Sensia Software LLC.
 Portions created by the Initial Developer are Copyright (C) 2014
 the Initial Developer. All Rights Reserved.
 
 Please Contact Alexandre Robin <alex.robin@sensiasoftware.com> or
 Mike Botts <mike.botts@botts-inc.net> for more information.
 
 Contributor(s): 
    Alexandre Robin <alex.robin@sensiasoftware.com>
 
******************************* END LICENSE BLOCK ***************************/

package org.vast.sensorML.metadata;

import java.util.List;


public interface IMetadataList<ItemType> extends List<ItemType>
{

    public abstract String getLocalId();


    public abstract void setLocalId(String localId);


    public abstract String getIdentifier();


    public abstract void setIdentifier(String identifier);


    public abstract String getLabel();


    public abstract void setLabel(String label);


    public abstract String getDescription();


    public abstract void setDescription(String description);
    
    
    public abstract String getCodespace();


    public abstract void setCodespace(String codespace);

}