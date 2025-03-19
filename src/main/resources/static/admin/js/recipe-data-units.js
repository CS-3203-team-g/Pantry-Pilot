// Unit management
const RecipeDataUnits = {
    getUnitIDByName(unitName) {
        if (!unitName) return null;
        const unit = RecipeDataCore.data.units.find(u => u.unitName === unitName);
        return unit ? unit.unitID : null;
    },

    getOrCreateUnit(unitName) {
        if (!RecipeDataCore.data.units) { RecipeDataCore.data.units = []; }
        const normalizedUnit = unitName.trim().toLowerCase();
        let existingUnit = RecipeDataCore.data.units.find(u =>
            u.unitName && u.unitName.trim().toLowerCase() === normalizedUnit
        );
        if (existingUnit) {
            return existingUnit.unitID;
        } else {
            let newUnitID = RecipeDataCore.data.units.length > 0 ? 
                Math.max(...RecipeDataCore.data.units.map(u => u.unitID)) + 1 : 1;
            let newUnit = {
                unitID: newUnitID,
                unitName: unitName.trim()
            };
            RecipeDataCore.data.units.push(newUnit);
            RecipeDataCore.updateJsonEditor();
            return newUnitID;
        }
    }
};

window.RecipeDataUnits = RecipeDataUnits;