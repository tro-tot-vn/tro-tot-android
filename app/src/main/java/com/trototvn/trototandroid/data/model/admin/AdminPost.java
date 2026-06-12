package com.trototvn.trototandroid.data.model.admin;

import com.trototvn.trototandroid.utils.Constants;

import java.util.List;

/**
 * A pending rental post awaiting moderation.
 * Shape mirrors backend GET api/admin/posts/pending (admin.service.ts listPostPending).
 * Plain POJO — passed to the detail screen as a Gson JSON string in a Bundle
 * (there is no get-pending-by-id endpoint).
 */
public class AdminPost {

    private int postId;
    private String title;
    private String description;
    private double price;
    private String streetNumber;
    private String street;
    private String city;
    private String district;
    private String ward;
    private String interiorCondition;
    private double acreage;
    private String createdAt;
    private String status;
    private String extendedAt;
    private List<MultimediaFile> multimediaFiles;
    private Owner owner;

    public int getPostId() {
        return postId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getWard() {
        return ward;
    }

    public String getInteriorCondition() {
        return interiorCondition;
    }

    public double getAcreage() {
        return acreage;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }

    public String getExtendedAt() {
        return extendedAt;
    }

    public List<MultimediaFile> getMultimediaFiles() {
        return multimediaFiles;
    }

    public Owner getOwner() {
        return owner;
    }

    /** Compose the full Vietnamese address (street number -> city). */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        appendPart(sb, streetNumber);
        appendPart(sb, street);
        appendPart(sb, ward);
        appendPart(sb, district);
        appendPart(sb, city);
        return sb.toString();
    }

    /** Short location used in list rows. */
    public String getShortLocation() {
        StringBuilder sb = new StringBuilder();
        appendPart(sb, district);
        appendPart(sb, city);
        return sb.toString();
    }

    private static void appendPart(StringBuilder sb, String part) {
        if (part != null && !part.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(part.trim());
        }
    }

    public static class MultimediaFile {
        private int fileId;
        private FileRef file;

        public int getFileId() {
            return fileId;
        }

        public FileRef getFile() {
            return file;
        }

        /** Backend-served file URL. */
        public String getUrl() {
            int id = file != null ? file.getFileId() : fileId;
            return Constants.BASE_URL + "api/files/" + id;
        }

        public boolean isVideo() {
            return file != null && "Video".equalsIgnoreCase(file.getFileType());
        }
    }

    public static class FileRef {
        private int fileId;
        private String fileType; // "Image" | "Video"
        private String createdAt;

        public int getFileId() {
            return fileId;
        }

        public String getFileType() {
            return fileType;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }

    public static class Owner {
        private int customerId;
        private String firstName;
        private String lastName;
        private String currentCity;
        private String currentDistrict;
        private String currentJob;
        private String avatar;
        private String joinedAt;
        private OwnerAccount account;

        public int getCustomerId() {
            return customerId;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getCurrentJob() {
            return currentJob;
        }

        public String getAvatar() {
            return avatar;
        }

        public String getJoinedAt() {
            return joinedAt;
        }

        public OwnerAccount getAccount() {
            return account;
        }

        public String getFullName() {
            String f = firstName != null ? firstName : "";
            String l = lastName != null ? lastName : "";
            return (f + " " + l).trim();
        }
    }

    public static class OwnerAccount {
        private int accountId;
        private String email;
        private String phone;

        public int getAccountId() {
            return accountId;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }
    }
}
