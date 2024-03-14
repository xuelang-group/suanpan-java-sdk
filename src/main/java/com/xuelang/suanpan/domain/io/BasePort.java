package com.xuelang.suanpan.domain.io;

/**
 * inPorts: [       // 输入桩
 *         {
 *           uuid: 'in1',
 *           type: 'data',
 *           subType: 'all',
 *           description: {
 *             zh_CN: '输入1'
 *           }
 *         }
 *       ]
 */
public abstract class BasePort {
    protected String uuid;
    protected String name;
    protected String type;
    protected String subType;
    protected String description;

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getSubType() {
        return subType;
    }

    public String getDescription() {
        return description;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        return this.uuid.equals(((BasePort) o).getUuid());
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
