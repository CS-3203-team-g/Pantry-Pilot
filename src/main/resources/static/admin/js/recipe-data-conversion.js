// Conversion Factor Management
const RecipeDataConversion = {
    getConversionFactors() {
        return RecipeDataCore.draftRecipe ? RecipeDataCore.draftIngredientUnits : 
            (RecipeDataCore.data.ingredientUnits || []);
    },

    addOrUpdateConversionFactor(ingredientID, unitID, conversionFactor) {
        if (!RecipeDataCore.data.ingredientUnits) {
            RecipeDataCore.data.ingredientUnits = [];
        }

        const parsedIngredientID = parseInt(ingredientID);
        const parsedUnitID = parseInt(unitID);
        const parsedFactor = parseFloat(conversionFactor);

        const existingIndex = RecipeDataCore.data.ingredientUnits.findIndex(cf => 
            cf.ingredientID === parsedIngredientID && 
            cf.unitID === parsedUnitID
        );

        if (existingIndex >= 0) {
            RecipeDataCore.data.ingredientUnits[existingIndex].conversionFactor = parsedFactor;
        } else {
            RecipeDataCore.data.ingredientUnits.push({
                ingredientID: parsedIngredientID,
                unitID: parsedUnitID,
                conversionFactor: parsedFactor
            });
        }

        RecipeDataCore.updateJsonEditor();
    },

    getConversionFactor(ingredientID, unitID) {
        ingredientID = parseInt(ingredientID);
        unitID = parseInt(unitID);

        const conversionFactors = this.getConversionFactors();
        const factor = conversionFactors.find(cf => 
            cf.ingredientID === ingredientID && 
            cf.unitID === unitID
        );

        return factor ? factor.conversionFactor : 1;
    },

    syncDraftConversionFactors() {
        if (RecipeDataCore.draftIngredientUnits.length === 0) return;
        
        RecipeDataCore.draftIngredientUnits.forEach(draftCF => {
            const existingIndex = RecipeDataCore.data.ingredientUnits.findIndex(cf => 
                cf.ingredientID === draftCF.ingredientID && 
                cf.unitID === draftCF.unitID
            );
            
            if (existingIndex >= 0) {
                RecipeDataCore.data.ingredientUnits[existingIndex] = draftCF;
            } else {
                RecipeDataCore.data.ingredientUnits.push(draftCF);
            }
        });
    }
};

window.RecipeDataConversion = RecipeDataConversion;